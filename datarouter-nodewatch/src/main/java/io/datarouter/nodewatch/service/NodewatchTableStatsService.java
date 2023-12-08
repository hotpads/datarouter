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
package io.datarouter.nodewatch.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.nodewatch.storage.binarydto.storagestats.table.TableStorageStatsBinaryDto;
import io.datarouter.nodewatch.storage.latesttablecount.DatarouterLatestTableCountDao;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCount;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientAndTableNames;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.tag.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class NodewatchTableStatsService{

	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private DatarouterLatestTableCountDao latestTableCountDao;
	@Inject
	private TableStorageStatsService tableStorageStatsService;

	public Scanner<TableStats> scanStats(){

		// Load input data
		Map<ClientAndTableNames,? extends PhysicalNode<?,?,?>> nodes = tableSamplerService.scanCountableNodes()
				.toMap(PhysicalNode::clientAndTableNames);
		Map<ClientAndTableNames,LatestTableCount> latestTableCounts = latestTableCountDao.scan()
				.toMap(LatestTableCount::getClientAndTableNames);
		Map<ClientAndTableNames,TableStorageStatsBinaryDto> tableStats = tableStorageStatsService.loadTableStats();

		// Compute relevant tables
		List<ClientAndTableNames> tableIds = Scanner.concat(nodes.keySet(), latestTableCounts.keySet())
				.sort(ClientAndTableNames.COMPARE_CLIENT_TABLE)
				.deduplicateConsecutive()
				.list();

		// Make stats
		Map<ClientAndTableNames,PhysicalNodeStats> physicalNodeStats = Scanner.of(nodes.values())
				.toMap(PhysicalNode::clientAndTableNames, this::makeNodeStats);
		Map<ClientAndTableNames,SamplerStats> samplerStats = Scanner.of(latestTableCounts.values())
				.toMap(LatestTableCount::getClientAndTableNames, this::makeSamplerStats);
		Map<ClientAndTableNames,StorageStats> storageStats = Scanner.of(tableIds)
				.concatOpt(tableId -> tableStorageStatsService.findStorageStats(
						nodes.get(tableId),
						tableStats.get(tableId),
						latestTableCounts.get(tableId)))
				.toMap(StorageStats::clientAndTableNames);

		return Scanner.of(tableIds)
				.map(clientAndTableNames -> new TableStats(
						clientAndTableNames,
						Optional.ofNullable(physicalNodeStats.get(clientAndTableNames)),
						Optional.ofNullable(samplerStats.get(clientAndTableNames)),
						Optional.ofNullable(storageStats.get(clientAndTableNames))));
	}

	private PhysicalNodeStats makeNodeStats(PhysicalNode<?,?,?> physicalNode){
		return new PhysicalNodeStats(
				physicalNode.clientAndTableNames(),
				physicalNode.getClientType().getName(),
				physicalNode.getFieldInfo().findTag().map(Tag::displayLowerCase).orElse("unknown"));
	}

	private SamplerStats makeSamplerStats(LatestTableCount latestTableCount){
		return new SamplerStats(
				latestTableCount.getClientAndTableNames(),
				latestTableCount.getNumRows(),
				Duration.ofMillis(latestTableCount.getCountTimeMs()),
				Duration.between(latestTableCount.getDateUpdated(), Instant.now()),
				latestTableCount.getNumSpans(),
				latestTableCount.getNumSlowSpans());
	}

	/*---------- records -----------*/

	public record PhysicalNodeStats(
			ClientAndTableNames clientAndTableNames,
			String clientTypeString,
			String tagString){

		public static final Comparator<PhysicalNodeStats> COMPARE_CLIENT_TYPE
				= Comparator.comparing(PhysicalNodeStats::clientTypeString);
		public static final Comparator<PhysicalNodeStats> COMPARE_TAG
				= Comparator.comparing(PhysicalNodeStats::tagString);
	}

	public record SamplerStats(
			ClientAndTableNames clientAndTableNames,
			long numRows,
			Duration countTime,
			Duration updatedAgo,
			long numSpans,
			long numSlowSpans){

		public static final Comparator<SamplerStats> COMPARE_NUM_ROWS
				= Comparator.comparing(SamplerStats::numRows);
		public static final Comparator<SamplerStats> COMPARE_COUNT_TIME
				= Comparator.comparing(SamplerStats::countTime);
		public static final Comparator<SamplerStats> COMPARE_UPDATED_AGO
				= Comparator.comparing(SamplerStats::updatedAgo);
		public static final Comparator<SamplerStats> COMPARE_NUM_SPANS
				= Comparator.comparing(SamplerStats::numSpans);
		public static final Comparator<SamplerStats> COMPARE_NUM_SLOW_SPANS
				= Comparator.comparing(SamplerStats::numSlowSpans);
	}

	public record StorageStats(
			ClientAndTableNames clientAndTableNames,
			long numBytes,
			Optional<Double> optYearlyStorageCostDollars,
			Optional<Double> optYearlyNodeCostDollars,
			Optional<Double> optYearlyTotalCostDollars){

		public static final Comparator<StorageStats> COMPARE_NUM_BYTES
				= Comparator.comparing(StorageStats::numBytes);
		public static final Comparator<StorageStats> COMPARE_YEARLY_STORAGE_COST
				= Comparator.comparing(
						StorageStats::optYearlyStorageCostDollars,
						optEmptyFirstThen(Comparator.naturalOrder()));
		public static final Comparator<StorageStats> COMPARE_YEARLY_NODE_COST
				= Comparator.comparing(
						StorageStats::optYearlyNodeCostDollars,
						optEmptyFirstThen(Comparator.naturalOrder()));
		public static final Comparator<StorageStats> COMPARE_YEARLY_TOTAL_COST
				= Comparator.comparing(
						StorageStats::optYearlyTotalCostDollars,
						optEmptyFirstThen(Comparator.naturalOrder()));
	}

	public record TableStats(
			ClientAndTableNames clientAndTableNames,
			Optional<PhysicalNodeStats> optPhysicalNodeStats,
			Optional<SamplerStats> optSamplerStats,
			Optional<StorageStats> optStorageStats){

		// Names
		public static final Comparator<TableStats> COMPARE_CLIENT
				= Comparator.comparing(TableStats::clientName);
		public static final Comparator<TableStats> COMPARE_TABLE
				= Comparator.comparing(TableStats::tableName);

		// PhysicalNode
		public static final Comparator<TableStats> COMPARE_CLIENT_TYPE = Comparator.comparing(
				TableStats::optPhysicalNodeStats,
				optEmptyFirstThen(PhysicalNodeStats.COMPARE_CLIENT_TYPE));
		public static final Comparator<TableStats> COMPARE_TAG = Comparator.comparing(
				TableStats::optPhysicalNodeStats,
				optEmptyFirstThen(PhysicalNodeStats.COMPARE_TAG));

		// LatestTableCount
		public static final Comparator<TableStats> COMPARE_NUM_ROWS = Comparator.comparing(
				TableStats::optSamplerStats,
				optEmptyFirstThen(SamplerStats.COMPARE_NUM_ROWS));
		public static final Comparator<TableStats> COMPARE_COUNT_TIME = Comparator.comparing(
				TableStats::optSamplerStats,
				optEmptyFirstThen(SamplerStats.COMPARE_COUNT_TIME));
		public static final Comparator<TableStats> COMPARE_UPDATED_AGO = Comparator.comparing(
				TableStats::optSamplerStats,
				optEmptyFirstThen(SamplerStats.COMPARE_UPDATED_AGO));
		public static final Comparator<TableStats> COMPARE_NUM_SPANS = Comparator.comparing(
				TableStats::optSamplerStats,
				optEmptyFirstThen(SamplerStats.COMPARE_NUM_SPANS));
		public static final Comparator<TableStats> COMPARE_NUM_SLOW_SPANS = Comparator.comparing(
				TableStats::optSamplerStats,
				optEmptyFirstThen(SamplerStats.COMPARE_NUM_SLOW_SPANS));

		// StorageStats
		public static final Comparator<TableStats> COMPARE_NUM_BYTES = Comparator.comparing(
				TableStats::optStorageStats,
				optEmptyFirstThen(StorageStats.COMPARE_NUM_BYTES));
		public static final Comparator<TableStats> COMPARE_YEARLY_STORAGE_COST = Comparator.comparing(
				TableStats::optStorageStats,
				optEmptyFirstThen(StorageStats.COMPARE_YEARLY_STORAGE_COST));

		public String clientName(){
			return clientAndTableNames.client();
		}

		public String tableName(){
			return clientAndTableNames.table();
		}
	}

	private static <T> Comparator<Optional<T>> optEmptyFirstThen(Comparator<? super T> valueComparator){
		return Comparator.comparing(
				optional -> optional.orElse(null),
				Comparator.nullsFirst(valueComparator));
	}

}
