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

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.KvString;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.nodewatch.service.TableStorageSummarizerDtos.ColumnSize;
import io.datarouter.nodewatch.service.TableStorageSummarizerDtos.ColumnSummary;
import io.datarouter.nodewatch.service.TableStorageSummarizerDtos.TableSummary;
import io.datarouter.nodewatch.util.PhysicalSortedNodeWrapper;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientAndTableNames;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.SortedStorage.PhysicalSortedStorageNode;
import io.datarouter.util.Counter;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.tuple.Range;

public class TableStorageSummarizer<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(TableStorageSummarizer.class);

	private final Supplier<Boolean> shouldStop;
	private final TableSamplerService tableSamplerService;
	private final PhysicalSortedStorageNode<PK,D,F> node;
	private final long limit;

	public TableStorageSummarizer(
			Supplier<Boolean> shouldStop,
			TableSamplerService tableSamplerService,
			PhysicalSortedStorageNode<PK,D,F> node,
			long limit){
		this.shouldStop = shouldStop;
		this.tableSamplerService = tableSamplerService;
		this.node = node;
		this.limit = limit;
	}

	public TableStorageSummarizer(
			Supplier<Boolean> shouldStop,
			TableSamplerService tableSamplerService,
			DatarouterNodes datarouterNodes,
			ClientAndTableNames clientAndTableNames,
			long limit){
		this(shouldStop,
				tableSamplerService,
				new PhysicalSortedNodeWrapper<PK,D,F>(datarouterNodes, clientAndTableNames).node,
				limit);
	}

	public TableSummary summarizeTable(){
		long maxSamples = Math.max(1, limit / Config.DEFAULT_RESPONSE_BATCH_SIZE);
		List<Range<PK>> ranges = tableSamplerService.scanTableRangesUsingTableSamples(node)
				.shuffle()
				.limit(maxSamples)
				.list();
		long rangeLimit = Math.max(Config.DEFAULT_RESPONSE_BATCH_SIZE, limit / ranges.size());
		TableSummary tableSummary = Scanner.of(ranges)
				.map(range -> summarizeTableRange(range, rangeLimit))
				.reduce(TableSummary::combine)
				.orElse(TableSummary.EMPTY);
		logger.warn("summarized {}", new KvString()
				.add("client", node.getClientId().getName())
				.add("table", node.getFieldInfo().getTableName())
				.add("overallLimit", limit, NumberFormatter::addCommas)
				.add("ranges", ranges.size(), NumberFormatter::addCommas)
				.add("perRangeLimit", rangeLimit, NumberFormatter::addCommas)
				.add("scanned", tableSummary.numRowsIncluded(), NumberFormatter::addCommas)
				.add("avgValuesSize", tableSummary.avgValueBytes(), ByteLength::toString));
		return tableSummary;
	}

	public TableSummary summarizeTableRange(Range<PK> range, long rangeLimit){
		var numItems = new AtomicLong();
		var nameBytesPerColumnCounter = new Counter<String>();
		var valueBytesPerColumnCounter = new Counter<String>();
		var fielder = node.getFieldInfo().getFielderSupplier().get();
		node.scan(range)
				.limit(rangeLimit)
				.advanceUntil($ -> shouldStop.get())
				.forEach(databean -> {
					numItems.incrementAndGet();
					fielder.getFields(databean).forEach(field -> {
						byte[] valueBytes = field.getValueBytes();
						int valueLength = valueBytes == null ? 0 : valueBytes.length;
						valueBytesPerColumnCounter.increment(
								field.getKey().getColumnName(),
								valueLength);
						// At least for HBase/Bigtable, we don't persist null cells, so don't count their names
						int keyLength = valueBytes == null ? 0 : field.getKey().getColumnNameBytes().length;
						nameBytesPerColumnCounter.increment(field.getKey().getColumnName(), keyLength);
					});
		});
		return Scanner.of(node.getFieldInfo().getFieldColumnNames())
				.map(columnName -> new ColumnSummary(
						columnName,
						new ColumnSize(
								ByteLength.ofBytes(nameBytesPerColumnCounter.get(columnName)),
								ByteLength.ofBytes(valueBytesPerColumnCounter.get(columnName)),
								numItems.get())))
				.listTo(counts -> new TableSummary(
						counts,
						numItems.get()));
	}
}
