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
import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths;
import io.datarouter.plugin.copytable.link.JobletTableProcessorLink;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessor;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorJoblet;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorJoblet.TableProcessorJobletParams;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorRegistry;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.SortedStorage.PhysicalSortedStorageNode;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;
import io.datarouter.types.Ulid;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.form.HtmlFormValidator;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import jakarta.inject.Inject;

public class JobletTableProcessorHandler extends BaseHandler{

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
	private CopyTableChangelogService changelogService;
	@Inject
	private TableProcessorRegistry processorRegistry;
	@Inject
	private DatarouterCopyTablePaths paths;
	@Inject
	private DatarouterJobletCreationExecutor datarouterJobletCreationExecutor;

	@Handler
	private <PK extends PrimaryKey<PK>, D extends Databean<PK,D>>
	Mav joblets(JobletTableProcessorLink link){

		Optional<String> nodeName = link.nodeName;
		Optional<String> processorName = link.processorName;
		Optional<Integer> scanBatchSize = link.scanBatchSize;
		Optional<String> executionOrder = link.executionOrder;
		Optional<String> submitAction = link.submitAction;
		boolean shouldValidate = submitAction.isPresent();
		List<String> possibleNodes = tableSamplerService.scanCountableNodes()//creating joblets from the samples
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

		var form = new HtmlForm(HtmlFormMethod.POST);
		form.addSelectField()
				.withLabel("Processor Name")
				.withName(JobletTableProcessorLink.P_processorName)
				.withValues(possibleProcessors)
				.withSelected(processorName.orElse(null));
		form.addSelectField()
				.withLabel("Node Name")
				.withName(JobletTableProcessorLink.P_nodeName)
				.withValues(possibleNodes)
				.withSelected(nodeName.orElse(null));
		form.addNumberField()
				.withLabel("Scan Batch Size")
				.withName(JobletTableProcessorLink.P_scanBatchSize)
				.withPlaceholder(DEFAULT_SCAN_BATCH_SIZE)
				.withValue(
						scanBatchSize.map(String::valueOf).orElse(null),
						shouldValidate && scanBatchSize.isPresent(),
						HtmlFormValidator::positiveInteger);
		form.addSelectField()
				.withLabel("Joblet Priority")
				.withName(JobletTableProcessorLink.P_executionOrder)
				.withValues(possibleJobletPriorities);
		form.addButton()
				.withLabel("Create Joblets")
				.withValue("joblets");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Table Processor - Joblets")
					.withContent(TableProcessorHtml.makeContent(
							paths.datarouter.tableProcessor.joblets,
							form))
					.buildMav();
		}

		String jobletQueueId = processorName.get();
		String jobletGroupId = Ulid.newValue();
		@SuppressWarnings("unchecked")
		PhysicalSortedStorageNode<PK,D,?> sourceNode = (PhysicalSortedStorageNode<PK,D,?>)nodes.getNode(nodeName.get());
		List<TableSample> samples = tableSamplerService.scanSamplesForNode(sourceNode)
				.list();
		TableSampleKey previousSampleKey = null;
		List<JobletPackage> jobletPackages = new ArrayList<>();
		long totalItemsProcessed = 1;
		long counter = 1;
		int actualScanBatchSize = scanBatchSize
				.orElse(DEFAULT_SCAN_BATCH_SIZE);
		long numJoblets = 0;
		for(TableSample sample : samples){
			PK fromKeyExclusive = TableSamplerTool.extractPrimaryKeyFromSampleKey(sourceNode, previousSampleKey);
			PK toKeyInclusive = TableSamplerTool.extractPrimaryKeyFromSampleKey(sourceNode, sample.getKey());
			var jobletPackage = createJobletPackage(
					jobletQueueId,
					jobletGroupId,
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
				jobletQueueId,
				jobletGroupId,
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
				.batch(10)
				.parallelUnordered(new Threads(datarouterJobletCreationExecutor, 8))
				.forEach(jobletService::submitJobletPackages);
		changelogService.recordChangelogForTableProcessor(
				getSessionInfo(),
				"Joblet",
				nodeName.get(),
				processorName.get());
		return pageFactory.message(
				request,
				"jobletsCreated=" + numJoblets + " totalSamplesProcessed=" + totalItemsProcessed);
	}

	private <PK extends PrimaryKey<PK>> JobletPackage createJobletPackage(
			String queueId,
			String groupId,
			String sourceNodeName,
			PK fromKeyExclusive,
			PK toKeyInclusive,
			int scanBatchSize,
			String processorName,
			long estNumDatabeans,
			long jobletId,
			long numJoblets,
			JobletPriority executionOrder){
		var jobletParams = new TableProcessorJobletParams(
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
				queueId,
				groupId,
				jobletParams);
	}

}
