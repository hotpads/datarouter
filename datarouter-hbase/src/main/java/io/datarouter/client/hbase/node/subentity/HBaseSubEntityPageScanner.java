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
package io.datarouter.client.hbase.node.subentity;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.config.DatarouterHBaseExecutors.DatarouterHbaseClientExecutor;
import io.datarouter.client.hbase.node.nonentity.HBaseNonEntityQueryBuilder;
import io.datarouter.client.hbase.node.nonentity.HBaseNonEntityResultParser;
import io.datarouter.client.hbase.util.HBaseResultComparator;
import io.datarouter.client.hbase.util.HBaseResultScannerTool;
import io.datarouter.client.hbase.util.HBaseScanBuilder;
import io.datarouter.client.hbase.util.HBaseTableTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.PagingScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.Require;
import io.datarouter.util.bytes.ByteRange;
import io.datarouter.util.tuple.Range;

public class HBaseSubEntityPageScanner<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private static final int DEFAULT_SCAN_BATCH_SIZE = 100;

	private final ClientType<?,?> clientType;
	private final ClientId clientId;
	private final String tableName;
	private final String nodeName;
	private final HBaseClientManager hBaseClientManager;
	private final DatarouterHbaseClientExecutor datarouterHbaseClientExecutor;
	private final EntityPartitioner<EK> partitioner;
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final HBaseNonEntityQueryBuilder<EK,PK,D> queryBuilder;
	private final HBaseResultComparator resultComparator;
	private final HBaseNonEntityResultParser<EK,PK,D,F> resultParser;

	public HBaseSubEntityPageScanner(
			ClientType<?,?> clientType,
			ClientId clientId,
			String tableName,
			String nodeName,
			HBaseClientManager hBaseClientManager,
			DatarouterHbaseClientExecutor datarouterHbaseClientExecutor,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			EntityPartitioner<EK> partitioner,
			HBaseNonEntityQueryBuilder<EK,PK,D> queryBuilder,
			HBaseResultComparator resultComparator,
			HBaseNonEntityResultParser<EK,PK,D,F> resultParser){
		this.clientType = clientType;
		this.clientId = clientId;
		this.tableName = tableName;
		this.nodeName = nodeName;
		this.hBaseClientManager = hBaseClientManager;
		this.datarouterHbaseClientExecutor = datarouterHbaseClientExecutor;
		this.fieldInfo = fieldInfo;
		this.partitioner = partitioner;
		this.queryBuilder = queryBuilder;
		this.resultComparator = resultComparator;
		this.resultParser = resultParser;
	}

	public Scanner<Result> scanResults(Range<PK> range, Config config, boolean keysOnly){
		Range<ByteRange> byteRange = range
				.map(EntityPrimaryKey::getEntityKey)
				.map(queryBuilder::getEkByteRange)
				//need to overscan and filter extra pks/databeans later
				.setStartInclusive(true)
				.setEndInclusive(true);
		int offset = config.optOffset().orElse(0);
		Integer subscanLimit = config.optLimit().map(limit -> offset + limit).orElse(null);
		int pageSize = config.optOutputBatchSize().orElse(DEFAULT_SCAN_BATCH_SIZE);
		boolean cacheBlocks = config.optScannerCaching().orElse(true);
		return partitioner.scanPrefixes(range)
				.collate(prefix -> scanResultsInByteRange(prefix, byteRange, pageSize, subscanLimit, cacheBlocks,
						keysOnly), resultComparator);
	}

	private Scanner<Result> scanResultsInByteRange(
			byte[] prefix,
			Range<ByteRange> range,
			int pageSize,
			Integer limit,
			boolean cacheBlocks,
			boolean keysOnly){
		if(range.isEmpty()){
			return Scanner.empty();
		}
		@SuppressWarnings("resource")
		ResultPagingScanner pagingScanner = new ResultPagingScanner(pageSize, prefix, range, limit, cacheBlocks,
				keysOnly);
		return pagingScanner
				.concatenate(Scanner::of)
				.prefetch(datarouterHbaseClientExecutor, pageSize);
	}

	private class ResultPagingScanner extends PagingScanner<ByteRange,Result>{
		private final byte[] prefix;
		private final Range<ByteRange> mutableRange;
		private final boolean keysOnly;
		private final Optional<Integer> limit;
		private final boolean cacheBlocks;
		private long numFetched;
		private volatile boolean closed;//volatile for prefetcher

		public ResultPagingScanner(
				int pageSize,
				byte[] prefix,
				Range<ByteRange> range,
				Integer limit,
				boolean cacheBlocks,
				boolean keysOnly){
			super(pageSize);
			this.prefix = prefix;
			this.mutableRange = range.clone();
			this.keysOnly = keysOnly;
			this.limit = Optional.ofNullable(limit);
			this.cacheBlocks = cacheBlocks;
			this.numFetched = 0;
			this.closed = false;
		}

		@Override
		protected ByteRange nextParam(Result lastSeenItem){
			if(lastSeenItem == null){
				return null;
			}
			byte[] rowWithoutPrefix = resultParser.rowWithoutPrefix(lastSeenItem.getRow());
			return new ByteRange(rowWithoutPrefix);
		}

		@Override
		protected List<Result> nextPage(ByteRange resumeFrom){
			Require.isFalse(closed, "don't call me, i'm closed");
			if(limit.isPresent() && numFetched >= limit.get()){
				return Collections.emptyList();
			}
			if(resumeFrom != null){
				mutableRange.setStart(resumeFrom);
				mutableRange.setStartInclusive(false);
			}
			int pageLimit = pageSize;
			if(limit.isPresent()){
				long numRemaining = limit.get() - numFetched;
				pageLimit = Math.min(pageSize, (int)numRemaining);
			}
			List<Result> page;
			try{
				page = getPageOfResults(prefix, mutableRange, keysOnly, pageLimit, cacheBlocks);
				numFetched += page.size();
				return page;
			}catch(IOException e){
				if(closed){
					return Collections.emptyList();
				}
				throw new RuntimeException(e);
			}
		}

		@Override
		public void close(){
			closed = true;
		}
	}

	private List<Result> getPageOfResults(
			byte[] prefix,
			Range<ByteRange> rowRange,
			boolean keysOnly,
			int limit,
			boolean cacheBlocks)
	throws IOException{
		Scan scan = new HBaseScanBuilder()
				.withPrefix(prefix)
				.withRange(rowRange)
				.withColumnPrefix(fieldInfo.getEntityNodePrefix())
				.withKeyOnly(keysOnly)
				.withLimit(limit)
				.withCacheBlocks(cacheBlocks)
				.build();
		try(Table table = hBaseClientManager.getTable(clientId, tableName);
			ResultScanner resultScanner = HBaseTableTool.getResultScanner(table, scan)){
			List<Result> results = HBaseResultScannerTool.resultScannerNext(resultScanner, limit);
			DatarouterCounters.incClientNodeCustom(
					clientType,
					"scan " + (keysOnly ? "key" : "row") + " numRows",
					clientId.getName(),
					nodeName,
					results.size());
			return results;
		}
	}

}
