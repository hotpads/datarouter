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
package io.datarouter.nodewatch.job;

import java.time.Instant;

import io.datarouter.instrumentation.tablecount.TableCountBatchDto;
import io.datarouter.instrumentation.tablecount.TableCountDto;
import io.datarouter.instrumentation.tablecount.TableCountPublisher;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.nodewatch.service.NodewatchTableStatsService;
import io.datarouter.nodewatch.service.NodewatchTableStatsService.PhysicalNodeStats;
import io.datarouter.nodewatch.service.NodewatchTableStatsService.SamplerStats;
import io.datarouter.nodewatch.service.NodewatchTableStatsService.StorageStats;
import io.datarouter.nodewatch.service.NodewatchTableStatsService.TableStats;
import io.datarouter.storage.config.properties.ServiceName;
import jakarta.inject.Inject;

public class LatestTableCountPublisherJob extends BaseJob{

	@Inject
	private TableCountPublisher publisher;
	@Inject
	private ServiceName serviceName;
	@Inject
	private NodewatchTableStatsService tableStatsService;

	@Override
	public void run(TaskTracker tracker){
		tableStatsService.scanStats()
				.include(tableStats -> tableStats.optPhysicalNodeStats().isPresent())
				.include(tableStats -> tableStats.optSamplerStats().isPresent())
				.map(this::toDto)
				.batch(50)
				.map(TableCountBatchDto::new)
				.forEach(publisher::add);
	}

	private TableCountDto toDto(TableStats stats){
		PhysicalNodeStats physicalNodeStats = stats.optPhysicalNodeStats().orElseThrow();
		SamplerStats samplerStats = stats.optSamplerStats().orElseThrow();
		Long numBytes = stats.optStorageStats()
				.map(StorageStats::numBytes)
				.orElse(null);
		Double dollarsPerYear = stats.optStorageStats()
				.flatMap(StorageStats::optYearlyTotalCostDollars)
				.orElse(null);
		Instant dateUpdated = stats.optSamplerStats()
				.map(SamplerStats::updatedAgo)
				.map(updatedAgo -> Instant.now().minus(updatedAgo))
				.orElse(null);
		return new TableCountDto(
				serviceName.get(),
				stats.clientName(),
				stats.tableName(),
				physicalNodeStats.clientTypeString(),
				physicalNodeStats.tagString(),
				samplerStats.numRows(),
				samplerStats.numSpans(),
				samplerStats.numSlowSpans(),
				samplerStats.countTime().toMillis(),
				dateUpdated,
				numBytes,
				dollarsPerYear);
	}

}
