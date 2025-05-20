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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.datarouter.joblet.config.DatarouterJobletExecutors.DatarouterJobletCreationExecutor;
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
import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths;
import io.datarouter.plugin.copytable.link.JobletCopyTableLink;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.SortedStorage.PhysicalSortedStorageNode;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.form.HtmlFormValidator;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import jakarta.inject.Inject;

public class JobletCopyTableHandler extends BaseHandler{

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
	private CopyTableChangelogService changelogRecorderService;
	@Inject
	private DatarouterCopyTablePaths paths;
	@Inject
	private DatarouterJobletCreationExecutor datarouterJobletCreationExecutor;

	@Handler
	private <PK extends PrimaryKey<PK>, D extends Databean<PK,D>>
	Mav joblets(JobletCopyTableLink link){

		Optional<String> sourceNodeName = link.sourceNodeName;
		Optional<String> targetNodeName = link.targetNodeName;
		Optional<Integer> optScanBatchSize = link.scanBatchSize;
		Optional<Integer> optPutBatchSize = link.putBatchSize;
		Optional<Boolean> skipInvalidDatabeans = link.skipInvalidDatabeans;
		Optional<String> submitAction = link.submitAction;
		boolean shouldValidate = submitAction.isPresent();
		List<String> possibleNodes = tableSamplerService.scanCountableNodes()
				.map(node -> node.getClientId().getName() + "." + node.getFieldInfo().getTableName())
				.append("")
				.sort()
				.list();
		var form = new HtmlForm(HtmlFormMethod.POST);
		form.addSelectField()
				.withLabel("Source Node Name")
				.withName(JobletCopyTableLink.P_sourceNodeName)
				.withValues(possibleNodes)
				.withSelected(sourceNodeName.orElse(null));
		form.addSelectField()
				.withLabel("Target Node Name")
				.withName(JobletCopyTableLink.P_targetNodeName)
				.withValues(possibleNodes)
				.withSelected(targetNodeName.orElse(null));
		form.addNumberField()
				.withLabel("Scan Batch Size")
				.withName(JobletCopyTableLink.P_scanBatchSize)
				.withPlaceholder(DEFAULT_SCAN_BATCH_SIZE)
				.withValue(
						optScanBatchSize.map(String::valueOf).orElse(null),
						shouldValidate && optScanBatchSize.isPresent(),
						HtmlFormValidator::positiveInteger);
		form.addNumberField()
				.withLabel("Put Batch Size")
				.withName(JobletCopyTableLink.P_putBatchSize)
				.withPlaceholder(DEFAULT_PUT_BATCH_SIZE)
				.withValue(
						optPutBatchSize.map(String::valueOf).orElse(null),
						shouldValidate && optPutBatchSize.isPresent(),
						HtmlFormValidator::positiveInteger);
		form.addCheckboxField()
				.withLabel("Skip Invalid Databeans")
				.withName(JobletCopyTableLink.P_skipInvalidDatabeans)
				.withChecked(DEFAULT_SKIP_INVALID_DATABEANS);
		form.addButton()
				.withLabel("Create Joblets")
				.withValue("joblets");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Copy Table - Joblets")
					.withContent(CopyTableHtml.makeContent(
							paths.datarouter.copyTable.joblets,
							form))
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
				.orElse(DEFAULT_SCAN_BATCH_SIZE);
		int putBatchSize = optPutBatchSize
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
				.batch(10)
				.parallelUnordered(new Threads(datarouterJobletCreationExecutor, 8))
				.forEach(jobletService::submitJobletPackages);
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
		var jobletParams = new CopyTableJobletParams(
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
				false,
				tableName,
				sourceNodeName,
				jobletParams);
	}

}
