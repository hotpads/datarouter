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
package io.datarouter.client.hbase.node.nonentity;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;

import io.datarouter.bytes.Bytes;
import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.config.DatarouterHBaseExecutors.DatarouterHbaseClientExecutor;
import io.datarouter.client.hbase.util.HBaseResultComparator;
import io.datarouter.client.hbase.util.HBaseResultScannerTool;
import io.datarouter.client.hbase.util.HBaseScanBuilder;
import io.datarouter.client.hbase.util.HBaseTableTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.PagingScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientTableNodeNames;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.ScannerConfigTool;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.serialize.fieldcache.EntityFieldInfo;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.Require;
import io.datarouter.util.tuple.Range;

public class HBaseReaderNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements MapStorageReader<PK,D>, SortedStorageReader<PK,D>{

	private static final int DEFAULT_SCAN_BATCH_SIZE = 100;
	private static final FirstKeyOnlyFilter FIRST_KEY_ONLY_FILTER = new FirstKeyOnlyFilter();
	private static final KeyOnlyFilter KEY_ONLY_FILTER = new KeyOnlyFilter();

	private final HBaseClientManager hBaseClientManager;
	private final ClientType<?,?> clientType;
	private final DatarouterHbaseClientExecutor datarouterHbaseClientExecutor;

	protected final ClientTableNodeNames clientTableNodeNames;
	protected final EntityFieldInfo<EK,E> entityFieldInfo;
	protected final EntityPartitioner<EK> partitioner;
	protected final HBaseNonEntityQueryBuilder<EK,PK,D> queryBuilder;
	private final HBaseResultComparator resultComparator;
	private final HBaseNonEntityResultParser<EK,PK,D,F> resultParser;

	public HBaseReaderNode(
			HBaseClientManager hBaseClientManager,
			EntityNodeParams<EK,E> entityNodeParams,
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			DatarouterHbaseClientExecutor datarouterHbaseClientExecutor){
		super(params, clientType);
		this.hBaseClientManager = hBaseClientManager;
		this.clientType = clientType;
		this.datarouterHbaseClientExecutor = datarouterHbaseClientExecutor;
		this.clientTableNodeNames = new ClientTableNodeNames(
				getFieldInfo().getClientId(),
				getFieldInfo().getTableName(),
				getName());
		this.entityFieldInfo = new EntityFieldInfo<>(entityNodeParams);
		this.partitioner = entityFieldInfo.getEntityPartitioner();
		this.queryBuilder = new HBaseNonEntityQueryBuilder<>(partitioner);
		this.resultComparator = new HBaseResultComparator(partitioner.getNumPrefixBytes());
		this.resultParser = new HBaseNonEntityResultParser<>(partitioner, getFieldInfo());
	}

	/*---------------------------- MapStorageReader -----------------------*/

	@Override
	public boolean exists(PK key, Config config){
		return getResults(Collections.singleton(key), config, true)
				.hasAny();
	}

	@Override
	public D get(PK key, Config config){
		return getResults(Collections.singleton(key), config, false)
				.map(resultParser::toDatabean)
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		return getResults(keys, config, false)
				.map(resultParser::toDatabean)
				.list();
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		return getResults(keys, config, true)
				.map(resultParser::toPk)
				.list();
	}

	/*---------------------------- get Results -----------------------------------*/

	private Scanner<Result> getResults(Collection<PK> keys, Config config, boolean keysOnly){
		if(keys == null || keys.isEmpty()){
			return Scanner.empty();
		}
		return Scanner.of(keys)
				.map(queryBuilder::getPkBytesWithPartition)
				.map(Get::new)
				.each(get -> configureKeyOnlyFilter(get, keysOnly))
				.batch(config.findInputBatchSize().orElse(100))
				.map(gets -> {
					try(Table table = getTable()){
						return HBaseTableTool.getUnchecked(table, gets);
					}catch(IOException e){
						throw new RuntimeException(e);
					}
				})
				.concat(Scanner::of)
				.exclude(Result::isEmpty);
	}

	private static void configureKeyOnlyFilter(Get get, boolean keysOnly){
		if(keysOnly){
			get.setFilter(new FilterList(FIRST_KEY_ONLY_FILTER, KEY_ONLY_FILTER));
		}
	}

	/*---------------------------- SortedStorageReader -----------------------------------*/

	@Override
	public Scanner<PK> scanKeys(Range<PK> range, Config config){
		return scanResults(range, config, true)
				.map(resultParser::toPk);
	}

	@Override
	public Scanner<PK> scanRangesKeys(Collection<Range<PK>> ranges, Config config){
		return scanRangesResults(ranges, config, true)
				.map(resultParser::toPk);
	}

	@Override
	public Scanner<D> scan(Range<PK> range, Config config){
		return scanResults(range, config, false)
				.map(resultParser::toDatabean);
	}

	@Override
	public Scanner<D> scanRanges(Collection<Range<PK>> ranges, Config config){
		return scanRangesResults(ranges, config, false)
				.map(resultParser::toDatabean);
	}

	/*---------------------------- scan Results -----------------------------------*/

	private Scanner<Result> scanRangesResults(Collection<Range<PK>> ranges, Config config, boolean keysOnly){
		Config subscanConfig = config.clone().setOffset(0);
		Scanner<Result> collated = Scanner.of(ranges)
				.collate(range -> scanResults(range, subscanConfig, keysOnly), resultComparator);
		return ScannerConfigTool.applyOffsetAndLimit(collated, config);
	}

	protected Scanner<Result> scanResults(Range<PK> range, Config config, boolean keysOnly){
		if(HBaseNonEntityQueryBuilder.isSingleRowRange(range)){
			return getResults(Collections.singleton(range.getStart()), config, keysOnly);
		}
		Range<Bytes> byteRange = range.map(queryBuilder::getPkByteRange);
		int offset = config.findOffset().orElse(0);
		Integer subscanLimit = config.findLimit().map(limit -> offset + limit).orElse(null);
		int pageSize = config.findOutputBatchSize().orElse(DEFAULT_SCAN_BATCH_SIZE);
		boolean prefetch = config.findScannerPrefetching().orElse(true);
		boolean cacheBlocks = config.findScannerCaching().orElse(true);
		Scanner<Result> collatedPartitions = partitioner.scanPrefixes(range)
				.collate(prefix -> scanResultsInByteRange(prefix, byteRange, pageSize, subscanLimit, prefetch,
						cacheBlocks, keysOnly), resultComparator);
		return ScannerConfigTool.applyOffsetAndLimit(collatedPartitions, config);
	}

	private Scanner<Result> scanResultsInByteRange(
			byte[] prefix,
			Range<Bytes> range,
			int pageSize,
			Integer limit,
			boolean prefetch,
			boolean cacheBlocks,
			boolean keysOnly){
		if(range.isEmpty()){
			return Scanner.empty();
		}
		@SuppressWarnings("resource")
		var pagingScanner = new ResultPagingScanner(pageSize, prefix, range, limit, cacheBlocks, keysOnly);
		Scanner<Result> results = pagingScanner
				.concat(Scanner::of);
		if(prefetch){
			results = results.prefetch(datarouterHbaseClientExecutor, pageSize);
		}
		return results;
	}

	private class ResultPagingScanner extends PagingScanner<Bytes,Result>{
		private final byte[] prefix;
		private final Range<Bytes> mutableRange;
		private final boolean keysOnly;
		private final Optional<Integer> limit;
		private final boolean cacheBlocks;
		private long numFetched;
		private volatile boolean closed;//volatile for prefetcher

		public ResultPagingScanner(
				int pageSize,
				byte[] prefix,
				Range<Bytes> range,
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
		protected Bytes nextParam(Result lastSeenItem){
			if(lastSeenItem == null){
				return null;
			}
			byte[] rowWithoutPrefix = resultParser.rowWithoutPrefix(lastSeenItem.getRow());
			return new Bytes(rowWithoutPrefix);
		}

		@Override
		protected List<Result> nextPage(Bytes resumeFrom){
			Require.isFalse(closed, "don't call me, i'm closed");
			if(limit.isPresent() && numFetched >= limit.get()){
				return List.of();
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
					return List.of();
				}
				throw new RuntimeException("", e);
			}
		}

		@Override
		public void close(){
			closed = true;
		}
	}

	private List<Result> getPageOfResults(
			byte[] prefix,
			Range<Bytes> rowRange,
			boolean keysOnly,
			int limit,
			boolean cacheBlocks)
	throws IOException{
		Scan scan = new HBaseScanBuilder()
				.withPrefix(prefix)
				.withRange(rowRange)
				.withFirstKeyOnly(keysOnly)
				.withLimit(limit)
				.withCacheBlocks(cacheBlocks)
				.build();
		try(Table table = getTable();
			ResultScanner resultScanner = HBaseTableTool.getResultScanner(table, scan)){
			List<Result> results = HBaseResultScannerTool.resultScannerNext(resultScanner, limit);
			countPage(keysOnly, results.size());
			return results;
		}
	}

	protected Table getTable(){
		return hBaseClientManager.getTable(getClientId(), clientTableNodeNames.getTableName());
	}

	public EntityFieldInfo<EK,E> getEntityFieldInfo(){
		return entityFieldInfo;
	}

	public ClientTableNodeNames getClientTableNodeNames(){
		return clientTableNodeNames;
	}

	public HBaseNonEntityResultParser<EK,PK,D,F> getResultParser(){
		return resultParser;
	}

	private void countPage(boolean keysOnly, int numResults){
		DatarouterCounters.incClientNodeCustom(
				clientType,
				"scan " + (keysOnly ? "key" : "row") + " numRows",
				getClientId().getName(),
				getName(),
				numResults);
	}

}
