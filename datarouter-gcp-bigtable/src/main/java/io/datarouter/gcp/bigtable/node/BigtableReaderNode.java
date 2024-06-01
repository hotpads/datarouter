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
package io.datarouter.gcp.bigtable.node;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.google.api.gax.rpc.ServerStream;
import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.models.Filters;
import com.google.cloud.bigtable.data.v2.models.Filters.Filter;
import com.google.cloud.bigtable.data.v2.models.Query;
import com.google.cloud.bigtable.data.v2.models.Row;
import com.google.protobuf.ByteString;

import io.datarouter.bytes.Bytes;
import io.datarouter.gcp.bigtable.service.BigtableClientManager;
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
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.util.Require;
import io.datarouter.util.tuple.Range;

public class BigtableReaderNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements MapStorageReader<PK,D>, SortedStorageReader<PK,D>{

	public static final Filter KEY_ONLY_FILTER = Filters.FILTERS.chain()
			.filter(Filters.FILTERS.limit().cellsPerRow(1))
			.filter(Filters.FILTERS.limit().cellsPerColumn(1))
			.filter(Filters.FILTERS.value().strip());

	public static final Filter LATEST_VERSION_FILTER = Filters.FILTERS
			.limit()
			.cellsPerColumn(1);

	private static final int DEFAULT_SCAN_BATCH_SIZE = 100;
	private static final Comparator<Row> RESULT_ROW_COMPARATOR = Row.compareByKey();
	// these are used for databeans with no values outside the PK. we fake a value as we need at least 1 cell in a row
	public static final byte[] DUMMY_COL_NAME_BYTES = new byte[]{0};
	public static final String DUMMY_COL_NAME = new String(DUMMY_COL_NAME_BYTES);
	public static final byte[] DUMMY_FIELD_VALUE = new byte[]{Byte.MIN_VALUE};

	protected final BigtableClientManager manager;
	protected final BigtableQueryBuilder<EK,PK,D> queryBuilder;
	private final ClientTableNodeNames clientTableNodeNames;
	private final BigtableResultParser<EK,PK,D,F> resultParser;

	public BigtableReaderNode(
			BigtableClientManager manager,
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType){
		super(params, clientType);
		this.manager = manager;
		this.clientTableNodeNames = new ClientTableNodeNames(
				getFieldInfo().getClientId(),
				getFieldInfo().getTableName(),
				getName());
		this.queryBuilder = new BigtableQueryBuilder<>();
		this.resultParser = new BigtableResultParser<>(getFieldInfo());
	}

	/*----------------------------- MapStorageReader ------------------------*/

	@Override
	public boolean exists(PK key, Config config){
		ByteString rowKey = toByteString(key);
		BigtableDataClient client = manager.getTableDataClient(clientTableNodeNames.getClientId());
		return client.exists(clientTableNodeNames.getTableName(), rowKey);
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

	/*--------------------- SortedStorageReader -----------------------------*/

	@Override
	public Scanner<PK> scanKeys(Range<PK> range, Config config){
		return scanRangesResults(Collections.singleton(range), config, true)
				.map(resultParser::toPk);
	}

	@Override
	public Scanner<PK> scanRangesKeys(Collection<Range<PK>> ranges, Config config){
		return scanRangesResults(ranges, config, true)
				.map(resultParser::toPk);
	}

	@Override
	public Scanner<D> scan(Range<PK> range, Config config){
		return scanRangesResults(Collections.singleton(range), config, false)
				.map(resultParser::toDatabean);
	}

	@Override
	public Scanner<D> scanRanges(Collection<Range<PK>> ranges, Config config){
		return scanRangesResults(ranges, config, false)
				.map(resultParser::toDatabean);
	}

	/*------------------------- build Results -------------------------------*/

	private Scanner<Row> getResults(Collection<PK> keys, Config config, boolean keysOnly){
		return Scanner.of(keys)
				.batch(config.findRequestBatchSize().orElse(100))
				.map(batch -> {
					Query query = Query.create(clientTableNodeNames.getTableName());
					if(keysOnly){
						query.filter(KEY_ONLY_FILTER);
					}else{
						query.filter(LATEST_VERSION_FILTER);
					}
					Scanner.of(batch)
							.map(this::toByteString)
							.forEach(item -> query.rowKey(item));
					return query;
				})
				.map(query -> {
					BigtableDataClient client = manager.getTableDataClient(clientTableNodeNames.getClientId());
					ServerStream<Row> stream = client
							.readRows(query);
					return stream.iterator();
				})
				.concat(Scanner::of);
	}

	protected Scanner<Row> scanRangesResults(Collection<Range<PK>> ranges, Config config, boolean keysOnly){
		return Scanner.of(ranges)
				.collate(range -> scanResults(range, config, keysOnly), RESULT_ROW_COMPARATOR);
	}

	protected Scanner<Row> scanResults(Range<PK> range, Config config, boolean keysOnly){
		if(BigtableQueryBuilder.isSingleRowRange(range)){
			return getResults(Collections.singleton(range.getStart()), config, keysOnly);
		}
		Range<Bytes> byteRange = range.map(queryBuilder::getPkByteRange);
		PK rangeStart = range.getStart();
		boolean startIsFullKey = range.hasStart()
				&& FieldTool.countNonNullLeadingFields(rangeStart.getFields()) == rangeStart.getFields().size();
		int offset = config.findOffset().orElse(0);
		Integer subscanLimit = config
				.findLimit()
				.map(limit -> offset + limit)
				.orElse(null);
		int pageSize = config.findResponseBatchSize().orElse(DEFAULT_SCAN_BATCH_SIZE);
		Scanner<Row> results = scanResultsInByteRange(
				byteRange,
				pageSize,
				subscanLimit,
				keysOnly,
				startIsFullKey);
		return ScannerConfigTool.applyOffsetAndLimit(results, config);
	}

	private Scanner<Row> scanResultsInByteRange(
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

	private class ResultPagingScanner extends PagingScanner<Bytes,Row>{

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
		protected Optional<Bytes> nextParam(Row lastSeenItem){
			if(lastSeenItem == null){
				return Optional.empty();
			}
			byte[] row = lastSeenItem.getKey().toByteArray();
			Bytes bytes = new Bytes(row);
			return Optional.of(bytes);
		}

		@Override
		protected List<Row> nextPage(Optional<Bytes> resumeFrom){
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
			List<Row> page = getPageOfResults(mutableRange, keysOnly, pageLimit, startIsFullKey);
			numFetched += page.size();
			return page;
		}

		@Override
		public void close(){
			closed = true;
		}
	}

	private List<Row> getPageOfResults(
			Range<Bytes> range,
			boolean keysOnly,
			int limit,
			boolean startIsFullKey){
		Query scan = new BigtableScanBuilder(clientTableNodeNames.getTableName())
				.withRange(range)
				.withFirstKeyOnly(keysOnly)
				.withLimit(limit)
				.withStartIsFullKey(startIsFullKey)
				.build();
		BigtableDataClient client = manager.getTableDataClient(clientTableNodeNames.getClientId());
		ServerStream<Row> stream = client.readRows(scan);
		return Scanner.of(stream.iterator())
				.list();
	}

	protected ByteString toByteString(PK key){
		return ByteString.copyFrom(queryBuilder.getPkBytes(key));
	}

}
