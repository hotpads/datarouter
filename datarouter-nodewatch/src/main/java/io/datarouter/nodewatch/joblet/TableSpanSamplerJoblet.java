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
package io.datarouter.nodewatch.joblet;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.joblet.DatarouterJobletCounters;
import io.datarouter.joblet.codec.BaseGsonJobletCodec;
import io.datarouter.joblet.model.BaseJoblet;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletType.JobletTypeBuilder;
import io.datarouter.nodewatch.config.DatarouterNodewatchSettingRoot;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJoblet.TableSpanSamplerJobletParams;
import io.datarouter.nodewatch.storage.tablesample.DatarouterTableSampleDao;
import io.datarouter.nodewatch.storage.tablesample.TableSample;
import io.datarouter.nodewatch.storage.tablesample.TableSampleKey;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import io.datarouter.storage.node.tableconfig.ClientTableEntityPrefixNameWrapper;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.lang.ObjectTool;

public class TableSpanSamplerJoblet extends BaseJoblet<TableSpanSamplerJobletParams>{
	private static final Logger logger = LoggerFactory.getLogger(TableSpanSamplerJoblet.class);

	public static final JobletType<TableSpanSamplerJobletParams> JOBLET_TYPE = new JobletTypeBuilder<>(
			"TableSpanSamplerJoblet", // TODO rename to TableSampler
			TableSpanSamplerJobletCodec::new,
			TableSpanSamplerJoblet.class)
			.withShortQueueName("TableSampler") //unnecessary shortQueueName
			.disableScaling()
			.withPollingPeriod(Duration.ofSeconds(30))
			.build();

	private static final Duration MAX_RUNNING_TIME = Duration.ofMinutes(10);

	@Inject
	private DatarouterTableSampleDao tableSampleDao;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatarouterJobletCounters datarouterJobletCounters;
	@Inject
	private DatarouterNodewatchSettingRoot nodewatchSettingRoot;

	private List<TableSample> samples;

	@Override
	public void process(){
		TableSample dbSample = tableSampleDao.get(params.endSample.getKey());
		if(dbSample == null){
			logger.warn("aborting because dbSample missing {}", params.endSample);
			return;
		}
		if(ObjectTool.notEquals(params.samplerId, dbSample.getSamplerId())){
			logger.warn("aborting because wrong samplerId={}, {}", params.samplerId, params.endSample);
			return;
		}
		//TODO make expiration a generic joblet feature
		Duration age = jobletRequest.getKey().getAge();
		if(ComparableTool.gt(age, TableSample.MAX_TIME_IN_QUEUE)){
			logger.warn("aborting expired joblet {} with age {}", jobletRequest, age);
			datarouterJobletCounters.incNumJobletsExpired(1);
			datarouterJobletCounters.incNumJobletsExpired(JOBLET_TYPE, 1);
			return;
			//the joblet creator will quickly create another one based on the stale dateScheduled
		}
		PhysicalNode<?,?,?> physicalNode = datarouterNodes.getPhysicalNodeForClientAndTable(
				params.nodeNames.getClientName(),
				params.nodeNames.getTableName());
		SortedStorageReaderNode<?,?,?> node = (PhysicalSortedStorageReaderNode<?,?,?>)physicalNode;
		Objects.requireNonNull(node, "node not found for " + params.nodeNames);
		//TODO replace strings with more formal client detection
		boolean clientSupportsOffsetting = physicalNode.getClientType().supportsOffsetSampling();
		Instant deadline = Instant.now().plus(MAX_RUNNING_TIME);
		samples = new TableSpanSampler<>(
				node,
				tableSampleDao,
				params.samplerId,
				params.nodeNames,
				params.startSampleKey,
				params.endSample,
				params.sampleEveryN,
				clientSupportsOffsetting && nodewatchSettingRoot.enableOffsetting.get(),
				params.batchSize,
				Instant.ofEpochMilli(params.createdTimeMs),
				params.scanUntilEnd,
				deadline)
				.call();
	}

	//for tests only
	public List<TableSample> getSamples(){
		return samples;
	}

	public static class TableSpanSamplerJobletParams{

		public final boolean scanUntilEnd;
		public final long createdTimeMs;
		public final int sampleEveryN;
		public final int batchSize; //TODO primitive after migration
		public final TableSampleKey startSampleKey;
		public final TableSample endSample;
		public final ClientTableEntityPrefixNameWrapper nodeNames;
		public final long samplerId;

		public TableSpanSamplerJobletParams(
				boolean scanUntilEnd,
				long createdTimeMs,
				int sampleEveryN,
				int batchSize,
				TableSampleKey startSampleKey,
				TableSample endSample,
				ClientTableEntityPrefixNameWrapper nodeNames,
				long samplerId){
			this.scanUntilEnd = scanUntilEnd;
			this.createdTimeMs = createdTimeMs;
			this.sampleEveryN = sampleEveryN;
			this.batchSize = batchSize;
			this.startSampleKey = startSampleKey;
			this.endSample = endSample;
			this.nodeNames = nodeNames;
			this.samplerId = samplerId;
		}

	}

	public static class TableSpanSamplerJobletCodec extends BaseGsonJobletCodec<TableSpanSamplerJobletParams>{

		public TableSpanSamplerJobletCodec(){
			super(TableSpanSamplerJobletParams.class);
		}

		@Override
		public int calculateNumItems(TableSpanSamplerJobletParams params){
			return 1;
		}

	}

}
