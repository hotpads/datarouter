package com.hotpads.datarouter.client.imp.hbase.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

import com.hotpads.datarouter.client.ClientTableNodeNames;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseSubEntityQueryBuilder;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseSubEntityResultParser;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.entity.SubEntitySortedMapStorageReaderNode;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.serialize.fieldcache.EntityFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.concurrent.Lazy;
import com.hotpads.util.core.io.RuntimeIOException;
import com.hotpads.util.core.iterable.scanner.Scanner;
import com.hotpads.util.core.iterable.scanner.batch.AsyncBatchLoaderScanner;
import com.hotpads.util.core.iterable.scanner.collate.PriorityQueueCollator;
import com.hotpads.util.core.iterable.scanner.iterable.SingleUseScannerIterable;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;
import com.hotpads.util.core.stream.StreamTool;

public class HBaseSubEntityReaderNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements HBasePhysicalNode<PK,D>,
		SubEntitySortedMapStorageReaderNode<EK,PK,D,F>{

	public static final int DEFAULT_ITERATE_BATCH_SIZE = Config.DEFAULT_ITERATE_BATCH_SIZE;

	private ClientTableNodeNames clientTableNodeNames;
	protected EntityFieldInfo<EK,E> entityFieldInfo;

	protected HBaseSubEntityQueryBuilder<EK,E,PK,D,F> queryBuilder;
	protected HBaseSubEntityResultParser<EK,E,PK,D,F> resultParser;

	/******************************* constructors ************************************/

	public HBaseSubEntityReaderNode(EntityNodeParams<EK,E> entityNodeParams, NodeParams<PK,D,F> params){
		super(params);
		this.clientTableNodeNames = new ClientTableNodeNames(getClientId().getName(), getTableName(), getName());
		this.entityFieldInfo = new EntityFieldInfo<>(entityNodeParams);
		this.queryBuilder = new HBaseSubEntityQueryBuilder<>(entityFieldInfo, fieldInfo);
		this.resultParser = new HBaseSubEntityResultParser<>(entityFieldInfo, fieldInfo);
	}


	/***************************** plumbing methods ***********************************/

	@Override
	public HBaseClient getClient(){
		return (HBaseClient)getRouter().getClient(getClientId().getName());
	}

	@Override
	public String getEntityNodePrefix(){
		return fieldInfo.getEntityNodePrefix();
	}

	/************************************ MapStorageReader methods ****************************/

	@Override
	public boolean exists(PK key, Config config) {
		//should probably make a getKey method
		return get(key, config) != null;
	}


	@Override
	public D get(final PK key, final Config config){
		if(key==null){
			return null;
		}
		return DrCollectionTool.getFirst(getMulti(DrListTool.wrap(key), Config.nullSafe(config)));
	}


	@Override
	public List<D> getMulti(final Collection<PK> pks, final Config config){
		if(DrCollectionTool.isEmpty(pks)){
			return new LinkedList<>();
		}
		return new HBaseMultiAttemptTask<>(new HBaseTask<List<D>>(getDatarouter(), getClientTableNodeNames(),
				"getMulti", Config.nullSafe(config)){
			@Override
			public List<D> hbaseCall(Table htable, HBaseClient client, ResultScanner managedResultScanner)
			throws Exception{
				DRCounters.incClientNodeCustom(client.getType(), "getMulti requested", getClientName(), getNodeName(),
						DrCollectionTool.size(pks));
				List<Get> gets = queryBuilder.getGets(pks, false);
				Result[] hbaseResults = htable.get(gets);
				List<D> databeans = resultParser.getDatabeansWithMatchingQualifierPrefixMulti(hbaseResults);
				DRCounters.incClientNodeCustom(client.getType(), "getMulti found", getClientName(), getNodeName(),
						DrCollectionTool.size(pks));
				return databeans;
			}
		}).call();
	}


	@Override
	public List<PK> getKeys(final Collection<PK> pks, final Config config) {
		if(DrCollectionTool.isEmpty(pks)){
			return new LinkedList<>();
		}
		return new HBaseMultiAttemptTask<>(new HBaseTask<List<PK>>(getDatarouter(), getClientTableNodeNames(),
				"getKeys", Config.nullSafe(config)){
				@Override
				public List<PK> hbaseCall(Table htable, HBaseClient client, ResultScanner managedResultScanner)
				throws Exception{
					DRCounters.incClientNodeCustom(client.getType(), "getKeys requested", getClientName(),
							getNodeName() , DrCollectionTool.size(pks));
					List<Get> gets = queryBuilder.getGets(pks, true);
					Result[] hbaseResults = htable.get(gets);
					List<PK> pks = resultParser.getPrimaryKeysWithMatchingQualifierPrefixMulti(hbaseResults);
					DRCounters.incClientNodeCustom(client.getType(), "getKeys found", getClientName(), getNodeName(),
							DrCollectionTool.size(pks));
					return pks;
				}
			}).call();
	}


	/************************* sorted **********************************/

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		return getWithPrefixes(DrListTool.wrap(prefix), wildcardLastField, config);
	}


	@Override
	public List<D> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField,
			final Config config){
		if(DrCollectionTool.isEmpty(prefixes)){
			return new LinkedList<>();
		}
		final Config nullSafeConfig = Config.nullSafe(config);

		//segment prefixes into single vs multi-row queries
		final List<PK> singleEntityPrefixes = new ArrayList<>();
		final List<PK> multiEntityPrefixes = new ArrayList<>();
		for(PK prefix : prefixes){
			if(queryBuilder.isSingleEkPrefixQuery(prefix, wildcardLastField)){
				singleEntityPrefixes.add(prefix);
			}else{
				multiEntityPrefixes.add(prefix);
			}
		}

		//execute the single-row queries in a big multi-Get
		List<D> singleEntityResults = new HBaseMultiAttemptTask<>(new HBaseTask<List<D>>(
				getDatarouter(), getClientTableNodeNames(), "getWithPrefixes", nullSafeConfig){
				@Override
				public List<D> hbaseCall(Table htable, HBaseClient client, ResultScanner managedResultScanner)
				throws Exception{
					List<Get> gets = queryBuilder.getPrefixGets(singleEntityPrefixes, wildcardLastField, config);
					Result[] hbaseRows = htable.get(gets);
					return resultParser.getDatabeansWithMatchingQualifierPrefixMulti(hbaseRows);
				}
			}).call();

		//execute the multi-row queries in individual Scans
		//TODO parallelize
		List<D> multiEntityResults = new ArrayList<>();
		for(final PK pkPrefix : multiEntityPrefixes){
			EK ekPrefix = pkPrefix.getEntityKey();//we already determined prefix is confied to the EK
			final List<Scan> allPartitionScans = queryBuilder.getPrefixScans(ekPrefix, wildcardLastField,
					nullSafeConfig);
			for(final Scan singlePartitionScan : allPartitionScans){
			List<D> singleScanResults = new HBaseMultiAttemptTask<>(new HBaseTask<List<D>>(
					getDatarouter(), getClientTableNodeNames(), "getWithPrefixes", nullSafeConfig){
					@Override
					public List<D> hbaseCall(Table htable, HBaseClient client, ResultScanner managedResultScanner)
					throws Exception{
						List<D> results = new ArrayList<>();
						managedResultScanner = htable.getScanner(singlePartitionScan);
						for(Result row : managedResultScanner){
							if(row.isEmpty()){
								continue;
							}
							//TODO compute a limit to pass here
							List<D> singleRowResults = resultParser.getDatabeansWithMatchingQualifierPrefix(row, null);
							results.addAll(singleRowResults);
							if(config.getLimit()!=null && results.size()>=config.getLimit()){
								break;
							}
						}
						return results;
					}
				}).call();
				multiEntityResults.addAll(singleScanResults);
			}
		}

		List<D> allResults = DrListTool.concatenate(singleEntityResults, multiEntityResults);
		Collections.sort(allResults);
		return allResults;
	}

	@Override
	public Iterable<PK> scanKeys(final Range<PK> range, final Config config){
		final Config nullSafeConfig = Config.nullSafe(config);
		if(nullSafeConfig.getLimit() != null && nullSafeConfig.getOffset() != null){
			nullSafeConfig.setLimit(nullSafeConfig.getLimit() + nullSafeConfig.getOffset());
		}
		final Range<PK> nullSafeRange = Range.nullSafe(range);
		//single row. use Get. gets all pks in entity. no way to limit rows
		if(queryBuilder.isSingleEntity(nullSafeRange)){
			List<PK> pks = new HBaseMultiAttemptTask<>(new HBaseTask<List<PK>>(getDatarouter(),
					getClientTableNodeNames(), "scanPksInEntity", nullSafeConfig){
				@Override
				public List<PK> hbaseCall(Table htable, HBaseClient client, ResultScanner managedResultScanner)
				throws Exception{
					Get get = queryBuilder.getSingleRowRange(nullSafeRange.getStart().getEntityKey(), nullSafeRange,
							true);
					Result result = htable.get(get);
					return new ArrayList<>(resultParser.getPrimaryKeysWithMatchingQualifierPrefix(result,
					nullSafeConfig.getLimit()));
				}
			}).call();
			return DrIterableTool.skip(pks, DrNumberTool.longValue(nullSafeConfig.getOffset()));
		}
		List<AsyncBatchLoaderScanner<PK>> scanners = queryBuilder.getPkScanners(this, nullSafeRange, config);
		SortedScanner<PK> collator = new PriorityQueueCollator<>(scanners, DrNumberTool.longValue(nullSafeConfig
				.getLimit()));
		collator.advanceBy(nullSafeConfig.getOffset());
		return new SingleUseScannerIterable<>(collator);
	}

	@Override
	public Iterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		return () -> StreamTool.flatten(ranges.stream().map(range -> streamKeys(range, config))).iterator();
	}

	@Override
	public Iterable<D> scan(final Range<PK> range, final Config config){
		final Config nullSafeConfig = Config.nullSafe(config);
		if(nullSafeConfig.getLimit() != null && nullSafeConfig.getOffset() != null){
			nullSafeConfig.setLimit(nullSafeConfig.getLimit() + nullSafeConfig.getOffset());
		}
		final Range<PK> nullSafeRange = Range.nullSafe(range);
		//single row. use Get. gets all databeans in entity. no way to limit rows
		if(queryBuilder.isSingleEntity(nullSafeRange)){
			List<D> databeans = new HBaseMultiAttemptTask<>(new HBaseTask<List<D>>(getDatarouter(),
					getClientTableNodeNames(), "scanInEntity", nullSafeConfig){
				@Override
				public List<D> hbaseCall(Table htable, HBaseClient client, ResultScanner managedResultScanner)
				throws Exception{
					Get get = queryBuilder.getSingleRowRange(nullSafeRange.getStart().getEntityKey(), nullSafeRange,
							false);
					Result result = htable.get(get);
					return resultParser.getDatabeansWithMatchingQualifierPrefix(result, nullSafeConfig.getLimit());
				}
			}).call();
			return DrIterableTool.skip(databeans, DrNumberTool.longValue(nullSafeConfig.getOffset()));
		}
		List<AsyncBatchLoaderScanner<D>> scanners = queryBuilder.getDatabeanScanners(this, nullSafeRange, config);
		SortedScanner<D> collator = new PriorityQueueCollator<>(scanners, DrNumberTool.longValue(nullSafeConfig
				.getLimit()));
		collator.advanceBy(nullSafeConfig.getOffset());
		return new SingleUseScannerIterable<>(collator);
	}

	@Override
	public Iterable<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		return () -> StreamTool.flatten(ranges.stream().map(range -> stream(range, config))).iterator();
	}


	/***************************** helper methods **********************************/

	/*
	 * internal method to fetch a single batch of hbase rows/keys.  only public so that iterators in other packages
	 * can use it
	 *
	 * warning: we cannot currently limit the number of databeans/pks, only hbase rows.  be aware that it will probably
	 * return more databeans/pks than iterateBatchSize
	 */
	public List<Result> getResultsInSubRange(final int partition, final Range<PK> pkRange, final boolean keysOnly,
			final Config config){
		final Config nullSafeConfig = Config.nullSafe(config);
		final String scanKeysVsRowsNumBatches = "scan " + (keysOnly ? "pk" : "entity") + " numBatches";
		final String scanKeysVsRowsNumRows = "scan " + (keysOnly ? "pk" : "entity") + " numRows";
		return new HBaseMultiAttemptTask<>(new HBaseTask<List<Result>>(getDatarouter(),
				getClientTableNodeNames(), scanKeysVsRowsNumBatches, nullSafeConfig){
			@Override
			public List<Result> hbaseCall(Table htable, HBaseClient client, ResultScanner managedResultScanner)
			throws Exception{
				Scan scan = queryBuilder.getScanForSubrange(partition, pkRange, config, keysOnly);
				managedResultScanner = htable.getScanner(scan);
				List<Result> results = new ArrayList<>();
				for(Result row : managedResultScanner){
					if(row.isEmpty()){
						continue;
					}
					results.add(row);
					if(config.getIterateBatchSize()!=null && results.size()>=config.getIterateBatchSize()){
						break;
					}
					if(config.getLimit()!=null && results.size()>=config.getLimit()){
						break;
					}
				}
				managedResultScanner.close();
				DRCounters.incClientNodeCustom(client.getType(), scanKeysVsRowsNumRows, getClientName(), getNodeName(),
						DrCollectionTool.size(results));
				return results;
			}
		}).call();
	}

	public class HBaseSubEntityResultScanner implements Scanner<Cell>{
		private final String scanKeysVsRowsNumBatches;
		private final String scanKeysVsRowsNumRows;
		private final Config config;

		private final int partition;
		private final Range<PK> pkRange;
		private final boolean keysOnly;

		private final Lazy<ResultScanner> hbaseResultScanner;

		private Cell[] currentBatch;
		private int currentBatchIndex = 0;

		public HBaseSubEntityResultScanner(String scanKeysVsRowsNumBatches, String scanKeysVsRowsNumRows,
				Config config, int partition, Range<PK> pkRange, boolean keysOnly){
			this.scanKeysVsRowsNumBatches = scanKeysVsRowsNumBatches;
			this.scanKeysVsRowsNumRows = scanKeysVsRowsNumRows;
			this.config = config;
			this.partition = partition;
			this.pkRange = pkRange;
			this.keysOnly = keysOnly;
			this.hbaseResultScanner = Lazy.of(() -> initResultScanner());
			this.currentBatch = null;
			this.currentBatchIndex = 0;
		}

		@Override
		public Cell getCurrent(){
			if(currentBatch == null){
				return null;
			}
			return currentBatch[currentBatchIndex];
		}

		@Override
		public boolean advance(){
			if(currentBatch != null && currentBatchIndex < currentBatch.length - 1){
				++currentBatchIndex;
				return true;
			}
			boolean foundMoreData = loadNextResult();
			return foundMoreData;

			//TODO move iterateBatchSize and limit logic elsewhere

//			if(config.getIterateBatchSize()!=null && results.size()>=config.getIterateBatchSize()){
//				break;
//			}
//			if(config.getLimit()!=null && results.size()>=config.getLimit()){
//				break;
//			}
		}

		private ResultScanner initResultScanner(){
			return new HBaseMultiAttemptTask<>(new HBaseTask<ResultScanner>(getDatarouter(),
					getClientTableNodeNames(), scanKeysVsRowsNumBatches, config){
				@Override
				public ResultScanner hbaseCall(Table htable, HBaseClient client, ResultScanner managedResultScanner)
				throws Exception{
					Scan scan = queryBuilder.getScanForSubrange(partition, pkRange, config, keysOnly);
					return htable.getScanner(scan);
				}
			}).call();
		}

		private boolean loadNextResult(){
			Result result;
			do{
				try{
					result = hbaseResultScanner.get().next();
					DRCounters.incClientNodeCustom(getClient().getType(), scanKeysVsRowsNumRows, getClient().getName(),
							getName());
				}catch(IOException e){
					throw new RuntimeIOException(e);
				}
				if(result == null){
					currentBatch = null;
					hbaseResultScanner.get().close();//necessary?
					return false;
				}
			}while(!result.isEmpty());

			//found a non-empty result
			currentBatch = result.rawCells();
			currentBatchIndex = 0;
			return true;
		}
	}


	/********************* get/set *******************************/

	public HBaseSubEntityResultParser<EK,E,PK,D,F> getResultParser(){
		return resultParser;
	}

	public ClientTableNodeNames getClientTableNodeNames(){
		return clientTableNodeNames;
	}

	public EntityFieldInfo<EK,E> getEntityFieldInfo(){
		return entityFieldInfo;
	}
}
