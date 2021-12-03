/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.plugin.copytable.web;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.model.JobletPackage;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.storage.tablesample.TableSample;
import io.datarouter.nodewatch.storage.tablesample.TableSampleKey;
import io.datarouter.nodewatch.util.TableSamplerTool;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorJoblet;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorJoblet.TableProcessorJobletParams;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorRegistry;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.SortedStorage.PhysicalSortedStorageNode;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;

public class JobletTableProcessorHandler extends BaseHandler{

	private static final String
			P_nodeName = "nodeName",
			P_processorName = "processorName",
			P_putBatchSize = "putBatchSize",
			P_submitAction = "submitAction";

	private static final int DEFAULT_BATCH_SIZE = 1_000;

	@Inject
	private DatarouterNodes nodes;
	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private JobletService jobletService;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private CopyTableChangelogRecorderService changelogRecorderService;
	@Inject
	private TableProcessorRegistry processorRegistry;

	@Handler(defaultHandler = true)
	private <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	Mav defaultHandler(
			@Param(P_nodeName) OptionalString nodeName,
			@Param(P_processorName) OptionalString processorName,
			@Param(P_putBatchSize) OptionalString putBatchSize,
			@Param(P_submitAction) OptionalString submitAction){
		String errorPutBatchSize = null;

		if(submitAction.isPresent()){
			try{
				if(putBatchSize.map(StringTool::nullIfEmpty).isPresent()){
					Integer.valueOf(putBatchSize.get());
				}
			}catch(Exception e){
				errorPutBatchSize = "Please specify an integer";
			}
		}

		List<String> possibleNodes = tableSamplerService.scanCountableNodes()
				.map(node -> node.getClientId().getName() + "." + node.getFieldInfo().getTableName())
				.append("")
				.sort()
				.list();
		List<String> possibleProcessors = Scanner.of(processorRegistry.getAll())
				.map(Class::getSimpleName)
				.append("")
				.sort()
				.list();

		var form = new HtmlForm()
				.withMethod("post");
		form.addSelectField()
				.withDisplay("Node Name")
				.withName(P_nodeName)
				.withValues(possibleNodes);
		form.addSelectField()
				.withDisplay("Processor Name")
				.withName(P_processorName)
				.withValues(possibleProcessors);
		form.addTextField()
				.withDisplay("Batch Size")
				.withError(errorPutBatchSize)
				.withName(P_putBatchSize)
				.withPlaceholder(DEFAULT_BATCH_SIZE + "")
				.withValue(putBatchSize.orElse(null));
		form.addButton()
				.withDisplay("Create Joblets")
				.withValue("anything");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Table Processor - Joblets")
					.withContent(Html.makeContent(form))
					.buildMav();
		}

		@SuppressWarnings("unchecked")
		PhysicalSortedStorageNode<PK,D,?> sourceNode = (PhysicalSortedStorageNode<PK,D,?>)nodes.getNode(nodeName.get());
		String tableName = sourceNode.getFieldInfo().getTableName();
		List<TableSample> samples = tableSamplerService.scanSamplesForNode(sourceNode).list();
		TableSampleKey previousSampleKey = null;
		List<JobletPackage> jobletPackages = new ArrayList<>();
		long numJoblets = samples.size() + 1;//+1 for databeans beyond the final sample
		long counter = 1;
		int batchSize = putBatchSize
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_BATCH_SIZE);
		for(TableSample sample : samples){
			PK fromKeyExclusive = TableSamplerTool.extractPrimaryKeyFromSampleKey(sourceNode, previousSampleKey);
			PK toKeyInclusive = TableSamplerTool.extractPrimaryKeyFromSampleKey(sourceNode, sample.getKey());
			var jobletPackage = createJobletPackage(
					tableName,
					nodeName.get(),
					fromKeyExclusive,
					toKeyInclusive,
					processorName.get(),
					batchSize,
					sample.getNumRows(),
					counter,
					numJoblets);
			jobletPackages.add(jobletPackage);
			++counter;
			previousSampleKey = sample.getKey();
		}
		//include any rows created since the last sample
		PK fromKeyExclusive = TableSamplerTool.extractPrimaryKeyFromSampleKey(sourceNode, previousSampleKey);
		var jobletPackage = createJobletPackage(
				tableName,
				nodeName.get(),
				fromKeyExclusive,
				null, //open-ended
				processorName.get(),
				batchSize,
				1, //we have no idea about the true estNumDatabeans
				counter,
				numJoblets);
		jobletPackages.add(jobletPackage);
		++counter;
		// shuffle as optimization to spread write load. could be optional
		Scanner.of(jobletPackages).shuffle().flush(jobletService::submitJobletPackages);
		changelogRecorderService.recordChangelogForTableProcessor(getSessionInfo(), "Joblet", nodeName.get(),
				processorName.get());
		return pageFactory.message(request, "created " + numJoblets + " joblets");
	}

	private <PK extends PrimaryKey<PK>> JobletPackage createJobletPackage(
			String tableName,
			String sourceNodeName,
			PK fromKeyExclusive,
			PK toKeyInclusive,
			String processorName,
			int putBatchSize,
			long estNumDatabeans,
			long jobletId,
			long numJoblets){
		TableProcessorJobletParams jobletParams = new TableProcessorJobletParams(
				sourceNodeName,
				fromKeyExclusive == null ? null : PrimaryKeyPercentCodecTool.encode(fromKeyExclusive),
				toKeyInclusive == null ? null : PrimaryKeyPercentCodecTool.encode(toKeyInclusive),
				processorName,
				putBatchSize,
				estNumDatabeans,
				jobletId,
				numJoblets);
		return JobletPackage.create(
				TableProcessorJoblet.JOBLET_TYPE,
				JobletPriority.DEFAULT,
				false,
				tableName,
				sourceNodeName,
				jobletParams);
	}

	private static class Html{

		public static ContainerTag<?> makeContent(HtmlForm htmlForm){
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			return div(
					h2("Table Processor - Joblets"),
					form,
					br())
					.withClass("container mt-3");
		}

	}

}
