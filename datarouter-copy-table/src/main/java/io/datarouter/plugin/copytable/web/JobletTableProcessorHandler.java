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
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.model.JobletPackage;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.storage.tablesample.TableSample;
import io.datarouter.nodewatch.storage.tablesample.TableSampleKey;
import io.datarouter.nodewatch.util.TableSamplerTool;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessor;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorJoblet;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorJoblet.TableProcessorJobletParams;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorRegistry;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.SortedStorage.PhysicalSortedStorageNode;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;

public class JobletTableProcessorHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(JobletTableProcessorHandler.class);

	private static final String
			P_nodeName = "nodeName",
			P_scanBatchSize = "scanBatchSize",
			P_processorName = "processorName",
			P_executionOrder = "executionOrder",
			P_submitAction = "submitAction";

	private static final int DEFAULT_SCAN_BATCH_SIZE = Config.DEFAULT_RESPONSE_BATCH_SIZE;

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
			@Param(P_nodeName) Optional<String> nodeName,
			@Param(P_scanBatchSize) Optional<String> scanBatchSize,
			@Param(P_processorName) Optional<String> processorName,
			@Param(P_executionOrder) Optional<String> executionOrder,
			@Param(P_submitAction) Optional<String> submitAction){
		String errorScanBatchSize = null;
		if(submitAction.isPresent()){
			try{
				if(scanBatchSize.map(StringTool::nullIfEmpty).isPresent()){
					Integer.valueOf(scanBatchSize.get());
				}
			}catch(Exception e){
				errorScanBatchSize = "Please specify an integer";
			}
		}

		List<String> possibleNodes = tableSamplerService.scanCountableNodes()
				.map(node -> node.getClientId().getName() + "." + node.getFieldInfo().getTableName())
				.append("")
				.sort()
				.list();
		List<String> possibleProcessors = processorRegistry.scan()
				.map(TableProcessor::getClass)
				.map(Class::getSimpleName)
				.append("")
				.sort()
				.list();
		List<String> possibleJobletPriorities = Scanner.of(JobletPriority.values())
				.map(priority -> priority.display)
				.append("")
				.sort()
				.list();

		var form = new HtmlForm()
				.withMethod("post");
		form.addSelectField()
				.withDisplay("Node Name")
				.withName(P_nodeName)
				.withValues(possibleNodes);
		form.addTextField()
				.withDisplay("Scan Batch Size")
				.withError(errorScanBatchSize)
				.withName(P_scanBatchSize)
				.withPlaceholder(DEFAULT_SCAN_BATCH_SIZE + "")
				.withValue(scanBatchSize.orElse(null));
		form.addSelectField()
				.withDisplay("Processor Name")
				.withName(P_processorName)
				.withValues(possibleProcessors);
		form.addSelectField()
				.withDisplay("Joblet Priority")
				.withName(P_executionOrder)
				.withValues(possibleJobletPriorities);
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
		List<TableSample> samples = tableSamplerService.scanSamplesForNode(sourceNode)
				.list();
		TableSampleKey previousSampleKey = null;
		List<JobletPackage> jobletPackages = new ArrayList<>();
		long totalItemsProcessed = 1;
		long counter = 1;
		int actualScanBatchSize = scanBatchSize
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_SCAN_BATCH_SIZE);
		long numJoblets = 0;
		for(TableSample sample : samples){
			PK fromKeyExclusive = TableSamplerTool.extractPrimaryKeyFromSampleKey(sourceNode, previousSampleKey);
			PK toKeyInclusive = TableSamplerTool.extractPrimaryKeyFromSampleKey(sourceNode, sample.getKey());
			var jobletPackage = createJobletPackage(
					tableName,
					nodeName.get(),
					fromKeyExclusive,
					toKeyInclusive,
					actualScanBatchSize,
					processorName.get(),
					sample.getNumRows(),
					counter,
					numJoblets,
					JobletPriority.BY_DISPLAY.from(executionOrder.get()).orElse(JobletPriority.DEFAULT));
			jobletPackages.add(jobletPackage);
			++numJoblets;
			counter++;
			totalItemsProcessed++;
			previousSampleKey = sample.getKey();
		}

		//include any rows created since the last sample
		PK fromKeyExclusive = TableSamplerTool.extractPrimaryKeyFromSampleKey(sourceNode, previousSampleKey);
		var jobletPackage = createJobletPackage(
				tableName,
				nodeName.get(),
				fromKeyExclusive,
				null, //open-ended
				actualScanBatchSize,
				processorName.get(),
				1, //we have no idea about the true estNumDatabeans
				counter,
				numJoblets,
				JobletPriority.BY_DISPLAY.from(executionOrder.get()).orElse(JobletPriority.DEFAULT));
		++numJoblets;
		jobletPackages.add(jobletPackage);
		totalItemsProcessed++;
		counter++; //  jobletPackage.size() == counter == numJoblets
		// shuffle as optimization to spread write load. could be optional
		Scanner.of(jobletPackages)
				.shuffle()
				.flush(jobletService::submitJobletPackages);
		changelogRecorderService.recordChangelogForTableProcessor(
				getSessionInfo(),
				"Joblet",
				nodeName.get(),
				processorName.get());
		return pageFactory.message(request, "jobletsCreated=" + numJoblets + " totalSamplesProcessed="
				+ totalItemsProcessed);
	}

	private <PK extends PrimaryKey<PK>> JobletPackage createJobletPackage(
			String tableName,
			String sourceNodeName,
			PK fromKeyExclusive,
			PK toKeyInclusive,
			int scanBatchSize,
			String processorName,
			long estNumDatabeans,
			long jobletId,
			long numJoblets,
			JobletPriority executionOrder){
		TableProcessorJobletParams jobletParams = new TableProcessorJobletParams(
				sourceNodeName,
				fromKeyExclusive == null ? null : PrimaryKeyPercentCodecTool.encode(fromKeyExclusive),
				toKeyInclusive == null ? null : PrimaryKeyPercentCodecTool.encode(toKeyInclusive),
				scanBatchSize,
				processorName,
				estNumDatabeans,
				jobletId,
				numJoblets,
				executionOrder.getExecutionOrder());
		return JobletPackage.create(
				TableProcessorJoblet.JOBLET_TYPE,
				executionOrder,
				false,
				tableName,
				sourceNodeName,
				jobletParams);
	}

	private static class Html{

		public static DivTag makeContent(HtmlForm htmlForm){
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
