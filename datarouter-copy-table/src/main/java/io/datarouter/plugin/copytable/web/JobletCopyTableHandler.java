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

import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.model.JobletPackage;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.storage.tablesample.TableSample;
import io.datarouter.nodewatch.storage.tablesample.TableSampleKey;
import io.datarouter.nodewatch.util.TableSamplerTool;
import io.datarouter.plugin.copytable.CopyTableJoblet;
import io.datarouter.plugin.copytable.CopyTableJoblet.CopyTableJobletParams;
import io.datarouter.scanner.Scanner;
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

public class JobletCopyTableHandler extends BaseHandler{

	private static final String
			P_sourceNodeName = "sourceNodeName",
			P_targetNodeName = "targetNodeName",
			P_scanBatchSize = "scanBatchSize",
			P_putBatchSize = "putBatchSize",
			P_skipInvalidDatabeans = "skipInvalidDatabeans",
			P_submitAction = "submitAction";

	private static final int DEFAULT_SCAN_BATCH_SIZE = 100;
	private static final int DEFAULT_PUT_BATCH_SIZE = 100;
	public static final boolean DEFAULT_SKIP_INVALID_DATABEANS = false;

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

	@Handler(defaultHandler = true)
	private <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	Mav defaultHandler(
			@Param(P_sourceNodeName) Optional<String> sourceNodeName,
			@Param(P_targetNodeName) Optional<String> targetNodeName,
			@Param(P_scanBatchSize) Optional<String> optScanBatchSize,
			@Param(P_putBatchSize) Optional<String> optPutBatchSize,
			@Param(P_skipInvalidDatabeans) Optional<Boolean> skipInvalidDatabeans,
			@Param(P_submitAction) Optional<String> submitAction){
		String errorScanBatchSize = null;
		String errorPutBatchSize = null;

		if(submitAction.isPresent()){
			try{
				if(optScanBatchSize.map(StringTool::nullIfEmpty).isPresent()){
					Integer.valueOf(optScanBatchSize.get());
				}
			}catch(Exception e){
				errorScanBatchSize = "Please specify an integer";
			}
			try{
				if(optPutBatchSize.map(StringTool::nullIfEmpty).isPresent()){
					Integer.valueOf(optPutBatchSize.get());
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
		var form = new HtmlForm()
				.withMethod("post");
		form.addSelectField()
				.withDisplay("Source Node Name")
				.withName(P_sourceNodeName)
				.withValues(possibleNodes);
		form.addSelectField()
				.withDisplay("Target Node Name")
				.withName(P_targetNodeName)
				.withValues(possibleNodes);
		form.addTextField()
				.withDisplay("Scan Batch Size")
				.withError(errorScanBatchSize)
				.withName(P_scanBatchSize)
				.withPlaceholder(DEFAULT_SCAN_BATCH_SIZE + "")
				.withValue(optScanBatchSize.orElse(null));
		form.addTextField()
				.withDisplay("Put Batch Size")
				.withError(errorPutBatchSize)
				.withName(P_putBatchSize)
				.withPlaceholder(DEFAULT_PUT_BATCH_SIZE + "")
				.withValue(optPutBatchSize.orElse(null));
		form.addCheckboxField()
				.withDisplay("Skip Invalid Databeans")
				.withName(P_skipInvalidDatabeans)
				.withChecked(DEFAULT_SKIP_INVALID_DATABEANS);
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
		int scanBatchSize = optScanBatchSize
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_SCAN_BATCH_SIZE);
		int putBatchSize = optPutBatchSize
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_PUT_BATCH_SIZE);
		for(TableSample sample : samples){
			PK fromKeyExclusive = TableSamplerTool.extractPrimaryKeyFromSampleKey(sourceNode, previousSampleKey);
			PK toKeyInclusive = TableSamplerTool.extractPrimaryKeyFromSampleKey(sourceNode, sample.getKey());
			jobletPackages.add(createJobletPackage(
					tableName,
					sourceNodeName.get(),
					targetNodeName.get(),
					fromKeyExclusive,
					toKeyInclusive,
					scanBatchSize,
					putBatchSize,
					sample.getNumRows(),
					counter,
					numJoblets,
					skipInvalidDatabeans.orElse(DEFAULT_SKIP_INVALID_DATABEANS)));
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
				scanBatchSize,
				putBatchSize,
				1, //we have no idea about the true estNumDatabeans
				counter,
				numJoblets,
				skipInvalidDatabeans.orElse(DEFAULT_SKIP_INVALID_DATABEANS)));
		++counter;
		// shuffle as optimization to spread write load.  could be optional
		Scanner.of(jobletPackages)
				.shuffle()
				.flush(jobletService::submitJobletPackages);
		changelogRecorderService.recordChangelog(
				getSessionInfo(),
				"Joblets",
				sourceNodeName.get(),
				targetNodeName.get());
		return pageFactory.message(request, "created " + numJoblets + " joblets");
	}

	private <PK extends PrimaryKey<PK>> JobletPackage createJobletPackage(
			String tableName,
			String sourceNodeName,
			String targetNodeName,
			PK fromKeyExclusive,
			PK toKeyInclusive,
			int scanBatchSize,
			int putBatchSize,
			long estNumDatabeans,
			long jobletId,
			long numJoblets,
			boolean skipInvalidDatabeans){
		CopyTableJobletParams jobletParams = new CopyTableJobletParams(
				sourceNodeName,
				targetNodeName,
				fromKeyExclusive == null ? null : PrimaryKeyPercentCodecTool.encode(fromKeyExclusive),
				toKeyInclusive == null ? null : PrimaryKeyPercentCodecTool.encode(toKeyInclusive),
				scanBatchSize,
				putBatchSize,
				estNumDatabeans,
				jobletId,
				numJoblets,
				skipInvalidDatabeans);
		return JobletPackage.create(
				CopyTableJoblet.JOBLET_TYPE,
				JobletPriority.DEFAULT,
				true,
				tableName,
				sourceNodeName,
				jobletParams);
	}

	private static class Html{

		public static DivTag makeContent(HtmlForm htmlForm){
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
