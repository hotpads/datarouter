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
package io.datarouter.client.hbase.node;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

import io.datarouter.bytes.Bytes;
import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.util.HBaseQueryBuilder;
import io.datarouter.client.hbase.util.HBaseReaderTool;
import io.datarouter.client.hbase.util.HBaseResultParser;
import io.datarouter.client.hbase.util.HBaseScanBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.entity.EntityKey;
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
	private static final Comparator<Result> RESULT_ROW_COMPARATOR
			= Comparator.comparing(Result::getRow, Arrays::compareUnsigned);

	private final HBaseClientManager hBaseClientManager;
	private final ClientType<?,?> clientType;

	protected final ClientTableNodeNames clientTableNodeNames;
	protected final EntityFieldInfo<EK,E> entityFieldInfo;
	protected final HBaseQueryBuilder<EK,PK,D> queryBuilder;
	private final HBaseResultParser<EK,PK,D,F> resultParser;

	public HBaseReaderNode(
			HBaseClientManager hBaseClientManager,
			EntityNodeParams<EK,E> entityNodeParams,
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType){
		super(params, clientType);
		this.hBaseClientManager = hBaseClientManager;
		this.clientType = clientType;
		this.clientTableNodeNames = new ClientTableNodeNames(
				getFieldInfo().getClientId(),
				getFieldInfo().getTableName(),
				getName());
		this.entityFieldInfo = new EntityFieldInfo<>(entityNodeParams);
		this.queryBuilder = new HBaseQueryBuilder<>();
		this.resultParser = new HBaseResultParser<>(getFieldInfo());
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
				.map(queryBuilder::getPkBytes)
				.map(Get::new)
				.each(get -> HBaseReaderTool.configureKeyOnlyFilter(get, keysOnly))
				.batch(config.findRequestBatchSize().orElse(100))
				.map(gets -> {
					try(Table table = getTable()){
						return HBaseReaderTool.getUnchecked(table, gets);
					}catch(IOException e){
						throw new RuntimeException(e);
					}
				})
				.concat(Scanner::of)
				.exclude(Result::isEmpty);
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

	// When scanning multiple ranges, we need to apply the offset/limit across all collated results.
	// This can mean throwing away many results from the first Ranges just to apply the correct offset.
	private Scanner<Result> scanRangesResults(Collection<Range<PK>> ranges, Config config, boolean keysOnly){
		Config subscanConfig = config.clone().setOffset(0);
		Scanner<Result> collated = Scanner.of(ranges)
				.collate(range -> scanResults(range, subscanConfig, keysOnly), RESULT_ROW_COMPARATOR);
		return ScannerConfigTool.applyOffsetAndLimit(collated, config);
	}

	protected Scanner<Result> scanResults(Range<PK> range, Config config, boolean keysOnly){
		if(HBaseQueryBuilder.isSingleRowRange(range)){
			return getResults(Collections.singleton(range.getStart()), config, keysOnly);
		}
		Range<Bytes> byteRange = range.map(queryBuilder::getPkByteRange);
		PK rangeStart = range.getStart();
		boolean startIsFullKey = range.hasStart()
				&& FieldTool.countNonNullLeadingFields(rangeStart.getFields()) == rangeStart.getFields().size();
		int offset = config.findOffset().orElse(0);
		Integer subscanLimit = config.findLimit().map(limit -> offset + limit).orElse(null);
		int pageSize = config.findResponseBatchSize().orElse(DEFAULT_SCAN_BATCH_SIZE);
		Scanner<Result> results = scanResultsInByteRange(
				byteRange,
				pageSize,
				subscanLimit,
				keysOnly,
				startIsFullKey);
		return ScannerConfigTool.applyOffsetAndLimit(results, config);
	}

	private Scanner<Result> scanResultsInByteRange(
			Range<Bytes> range,
			int pageSize,
			Integer limit,
			boolean keysOnly,
			boolean startIsFullKey){
		if(range.isEmpty()){
			return Scanner.empty();
		}
		@SuppressWarnings("resource")
		var pagingScanner = new ResultPagingScanner(pageSize, range, limit, keysOnly, startIsFullKey);
		return pagingScanner
				.concat(Scanner::of);
	}

	private class ResultPagingScanner extends PagingScanner<Bytes,Result>{
		private final Range<Bytes> mutableRange;
		private final boolean keysOnly;
		private final Optional<Integer> limit;
		private long numFetched;
		private boolean startIsFullKey;
		private volatile boolean closed;//volatile for prefetcher

		public ResultPagingScanner(
				int pageSize,
				Range<Bytes> range,
				Integer limit,
				boolean keysOnly,
				boolean startIsFullKey){
			super(pageSize);
			this.startIsFullKey = startIsFullKey;
			this.mutableRange = range.clone();
			this.keysOnly = keysOnly;
			this.limit = Optional.ofNullable(limit);
			this.numFetched = 0;
			this.closed = false;
		}

		@Override
		protected Optional<Bytes> nextParam(Result lastSeenItem){
			if(lastSeenItem == null){
				return Optional.empty();
			}
			byte[] row = lastSeenItem.getRow();
			Bytes bytes = new Bytes(row);
			return Optional.of(bytes);
		}

		@Override
		protected List<Result> nextPage(Optional<Bytes> resumeFrom){
			Require.isFalse(closed, "don't call me, i'm closed");
			if(limit.isPresent() && numFetched >= limit.get()){
				return List.of();
			}
			if(resumeFrom.isPresent()){
				mutableRange.setStart(resumeFrom.get());
				mutableRange.setStartInclusive(false);
				startIsFullKey = true;
			}
			int pageLimit = pageSize;
			if(limit.isPresent()){
				long numRemaining = limit.get() - numFetched;
				pageLimit = Math.min(pageSize, (int)numRemaining);
			}
			List<Result> page;
			try{
				page = getPageOfResults(mutableRange, keysOnly, pageLimit, startIsFullKey);
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
			Range<Bytes> rowRange,
			boolean keysOnly,
			int limit,
			boolean startIsFullKey)
	throws IOException{
		Scan scan = new HBaseScanBuilder()
				.withRange(rowRange)
				.withFirstKeyOnly(keysOnly)
				.withLimit(limit)
				.withStartIsFullKey(startIsFullKey)
				.build();
		try(Table table = getTable();
			ResultScanner resultScanner = HBaseReaderTool.getResultScanner(table, scan)){
			List<Result> results = HBaseReaderTool.resultScannerNext(resultScanner, limit);
			countPage(keysOnly, results.size());
			return results;
		}
	}

	protected Table getTable(){
		return hBaseClientManager.getTable(getClientId(), clientTableNodeNames.getTableName());
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
