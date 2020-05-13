/**
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
import java.util.Objects;

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
import io.datarouter.plugin.copytable.CopyTableConfiguration;
import io.datarouter.plugin.copytable.CopyTableJoblet;
import io.datarouter.plugin.copytable.CopyTableJoblet.CopyTableJobletParams;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.SortedStorage.PhysicalSortedStorageNode;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalBoolean;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;

public class JobletCopyTableHandler extends BaseHandler{

	private static final String
			P_sourceNodeName = "sourceNodeName",
			P_targetNodeName = "targetNodeName",
			P_filterName = "filterName",
			P_processorName = "processorName",
			P_autoResume = "autoResume",
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
	private CopyTableConfiguration copyTableConfiguration;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private CopyTableChangelogRecorderService changelogRecorderService;

	@Handler(defaultHandler = true)
	private <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	Mav defaultHandler(
			@Param(P_sourceNodeName) OptionalString sourceNodeName,
			@Param(P_targetNodeName) OptionalString targetNodeName,
			@Param(P_filterName) OptionalString filterName,
			@Param(P_processorName) OptionalString processorName,
			@Param(P_autoResume) OptionalBoolean autoResume,
			@Param(P_putBatchSize) OptionalString putBatchSize,
			@Param(P_submitAction) OptionalString submitAction){
		String errorSourceNode = null;
		String errorTargetNode = null;
		String errorFilterName = null;
		String errorPutBatchSize = null;

		if(submitAction.isPresent()){
			try{
				Objects.requireNonNull(nodes.getNode(sourceNodeName.get()));
			}catch(Exception e){
				errorSourceNode = StringTool.isEmpty(sourceNodeName.get())
						? "Please specify source node"
						: "Unknown sourceNode: " + sourceNodeName.get();
			}
			try{
				Objects.requireNonNull(nodes.getNode(targetNodeName.get()));
			}catch(Exception e){
				errorTargetNode = StringTool.isEmpty(targetNodeName.get())
						? "Please specify target node"
						: "Unknown targetNode: " + targetNodeName.get();
			}
			try{
				copyTableConfiguration.getValidFilter(filterName);
			}catch(Exception e){
				errorFilterName = "Unknown filter: " + filterName.get();
			}
			try{
				if(putBatchSize.map(StringTool::nullIfEmpty).isPresent()){
					Integer.valueOf(putBatchSize.get());
				}
			}catch(Exception e){
				errorPutBatchSize = "Please specify an integer";
			}
		}

		var form = new HtmlForm()
				.withMethod("post");
		form.addTextField()
				.withDisplay("Source Node Name")
				.withError(errorSourceNode)
				.withName(P_sourceNodeName)
				.withPlaceholder("client.TableName")
				.withValue(sourceNodeName.orElse(null));
		form.addTextField()
				.withDisplay("Target Node Name")
				.withError(errorTargetNode)
				.withName(P_targetNodeName)
				.withPlaceholder("client.TableName")
				.withValue(targetNodeName.orElse(null));
		form.addTextField()
				.withDisplay("Filter Name")
				.withError(errorFilterName)
				.withName(P_filterName)
				.withPlaceholder("filterName")
				.withValue(filterName.orElse(null));
		form.addTextField()
				.withDisplay("Processor Name")
				.withName(P_processorName)
				.withValue(processorName.orElse(null));
		form.addCheckboxField()
				.withDisplay("Auto-resume")
				.withName(P_autoResume)
				.withChecked(autoResume.orElse(false));
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
					.withTitle("Copy Table - Joblets")
					.withContent(Html.makeContent(form))
					.buildMav();
		}

		@SuppressWarnings("unchecked")
		PhysicalSortedStorageNode<PK,D,?> sourceNode = (PhysicalSortedStorageNode<PK,D,?>)nodes.getNode(sourceNodeName
				.get());
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
			jobletPackages.add(createJobletPackage(
					tableName,
					sourceNodeName.get(),
					targetNodeName.get(),
					fromKeyExclusive,
					toKeyInclusive,
					filterName.map(StringTool::nullIfEmpty).orElse(null),
					processorName.map(StringTool::nullIfEmpty).orElse(null),
					autoResume.orElse(false),
					batchSize,
					sample.getNumRows(),
					counter,
					numJoblets));
			++counter;
			previousSampleKey = sample.getKey();
		}
		//include any rows created since the last sample
		PK fromKeyExclusive = TableSamplerTool.extractPrimaryKeyFromSampleKey(sourceNode, previousSampleKey);
		jobletPackages.add(createJobletPackage(
				tableName,
				sourceNodeName.get(),
				targetNodeName.get(),
				fromKeyExclusive,
				null, //open-ended
				filterName.map(StringTool::nullIfEmpty).orElse(null),
				processorName.map(StringTool::nullIfEmpty).orElse(null),
				autoResume.orElse(false),
				batchSize,
				1, //we have no idea about the true estNumDatabeans
				counter,
				numJoblets));
		++counter;
		// shuffle as optimization to spread write load.  could be optional
		jobletService.submitJobletPackages(CollectionTool.shuffleCopy(jobletPackages));
		changelogRecorderService.recordChangelog(getSessionInfo(), "Joblet", sourceNodeName.get(), targetNodeName
				.get());
		return pageFactory.message(request, "created " + numJoblets + " joblets");
	}

	private <PK extends PrimaryKey<PK>> JobletPackage createJobletPackage(
			String tableName,
			String sourceNodeName,
			String targetNodeName,
			PK fromKeyExclusive,
			PK toKeyInclusive,
			String filterName,
			String processorName,
			boolean autoResume,
			int putBatchSize,
			long estNumDatabeans,
			long jobletId,
			long numJoblets){
		CopyTableJobletParams jobletParams = new CopyTableJobletParams(
				sourceNodeName,
				targetNodeName,
				fromKeyExclusive == null ? null : PrimaryKeyPercentCodecTool.encode(fromKeyExclusive),
				toKeyInclusive == null ? null : PrimaryKeyPercentCodecTool.encode(toKeyInclusive),
				filterName,
				processorName,
				autoResume,
				putBatchSize,
				estNumDatabeans,
				jobletId,
				numJoblets);
		return JobletPackage.create(CopyTableJoblet.JOBLET_TYPE, JobletPriority.DEFAULT, true, tableName,
				sourceNodeName, jobletParams);
	}

	private static class Html{

		public static ContainerTag makeContent(HtmlForm htmlForm){
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			return div(
					h2("Copy Table - Joblets"),
					form,
					br())
					.withClass("container mt-3");
		}

	}

}
