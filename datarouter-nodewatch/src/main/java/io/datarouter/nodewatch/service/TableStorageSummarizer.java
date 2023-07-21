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
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.nodewatch.util.PhysicalSortedNodeWrapper;
import io.datarouter.scanner.Scanner;
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
			String clientName,
			String tableName,
			long limit){
		this(shouldStop,
				tableSamplerService,
				new PhysicalSortedNodeWrapper<PK,D,F>(datarouterNodes, clientName, tableName).node,
				limit);
	}

	public record ColumnSize(
			ByteLength nameBytes,
			ByteLength valueBytes,
			long numRowsIncluded){

		public ByteLength totalBytes(){
			return ByteLength.sum(nameBytes, valueBytes);
		}

		public ByteLength avgNameBytes(){
			double avg = (double)nameBytes.toBytes() / (double) numRowsIncluded;
			return ByteLength.ofBytes((long)avg);
		}

		public ByteLength avgValueBytes(){
			double avg = (double)valueBytes.toBytes() / (double) numRowsIncluded;
			return ByteLength.ofBytes((long)avg);
		}

		public ByteLength extrapolateTotalNameBytes(long totalTableRows){
			double multiplier = (double)totalTableRows / (double) numRowsIncluded;
			long estTotalBytes = (long)(nameBytes().toBytes() * multiplier);
			return ByteLength.ofBytes(estTotalBytes);
		}

		public ByteLength extrapolateTotalValueBytes(long totalTableRows){
			double multiplier = (double)totalTableRows / (double) numRowsIncluded;
			long estTotalBytes = (long)(valueBytes().toBytes() * multiplier);
			return ByteLength.ofBytes(estTotalBytes);
		}

		public static ColumnSize combine(ColumnSize first, ColumnSize second){
			return new ColumnSize(
					ByteLength.sum(first.nameBytes, second.nameBytes),
					ByteLength.sum(first.valueBytes, second.valueBytes),
					first.numRowsIncluded + second.numRowsIncluded);
		}
	}

	public record ColumnSummary(
			String name,
			ColumnSize size){
	}

	public record TableSummary(
			List<ColumnSummary> columnSummaries,
			long numRowsIncluded){

		public static final TableSummary EMPTY = new TableSummary(List.of(), 0);

		public ByteLength totalNameBytes(){
			return Scanner.of(columnSummaries)
					.map(ColumnSummary::size)
					.map(ColumnSize::nameBytes)
					.listTo(ByteLength::sum);
		}

		public ByteLength totalValueBytes(){
			return Scanner.of(columnSummaries)
					.map(ColumnSummary::size)
					.map(ColumnSize::valueBytes)
					.listTo(ByteLength::sum);
		}

		public ByteLength extrapolateNameSize(long totalTableRows){
			double multiplier = (double)totalTableRows / (double) numRowsIncluded;
			long estTotalBytes = (long)(totalNameBytes().toBytes() * multiplier);
			return ByteLength.ofBytes(estTotalBytes);
		}

		public ByteLength extrapolateValueSize(long totalTableRows){
			double multiplier = (double)totalTableRows / (double) numRowsIncluded;
			long estTotalBytes = (long)(totalValueBytes().toBytes() * multiplier);
			return ByteLength.ofBytes(estTotalBytes);
		}

		public ByteLength avgNameBytes(){
			double avg = (double)totalNameBytes().toBytes() / numRowsIncluded;
			return ByteLength.ofBytes((long)avg);
		}

		public ByteLength avgValueBytes(){
			double avg = (double)totalValueBytes().toBytes() / numRowsIncluded;
			return ByteLength.ofBytes((long)avg);
		}

		public ByteLength avgTotalBytes(){
			return ByteLength.sum(avgNameBytes(), avgValueBytes());
		}

		public static TableSummary combine(TableSummary first, TableSummary second){
			if(first.columnSummaries.isEmpty()){
				return second;
			}
			if(second.columnSummaries.isEmpty()){
				return first;
			}
			long totalRows = first.numRowsIncluded + second.numRowsIncluded;
			Map<String,ColumnSummary> firstByName = Scanner.of(first.columnSummaries)
					.toMap(ColumnSummary::name);
			return Scanner.of(second.columnSummaries)
					.map(col -> new ColumnSummary(
							col.name(),
							ColumnSize.combine(col.size(), firstByName.get(col.name()).size())))
					.listTo(cols -> new TableSummary(cols, totalRows));
		}
	}

	public TableSummary summarize(){
		long maxSamples = Math.max(1, limit / Config.DEFAULT_RESPONSE_BATCH_SIZE);
		List<Range<PK>> ranges = tableSamplerService.scanTableRangesUsingTableSamples(node)
				.shuffle()
				.limit(maxSamples)
				.list();
		long rangeLimit = Math.max(Config.DEFAULT_RESPONSE_BATCH_SIZE, limit / ranges.size());
		TableSummary tableSummary = Scanner.of(ranges)
				.map(range -> summarizeRange(range, rangeLimit))
				.reduce(TableSummary::combine)
				.orElse(TableSummary.EMPTY);
		logger.warn(
				"summarized client={}, table={}, overallLimit={}, ranges={}"
						+ ", perRangeLimit={}, scanned={}, avgValuesSize={}",
				node.getClientId().getName(),
				node.getFieldInfo().getTableName(),
				NumberFormatter.addCommas(limit),
				NumberFormatter.addCommas(ranges.size()),
				NumberFormatter.addCommas(rangeLimit),
				NumberFormatter.addCommas(tableSummary.numRowsIncluded),
				tableSummary.avgValueBytes());
		return tableSummary;
	}

	public TableSummary summarizeRange(Range<PK> range, long rangeLimit){
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
