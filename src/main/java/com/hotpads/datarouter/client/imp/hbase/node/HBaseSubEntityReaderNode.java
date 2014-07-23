package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.batching.entity.HBaseEntityDatabeanBatchLoader;
import com.hotpads.datarouter.client.imp.hbase.batching.entity.HBaseEntityPrimaryKeyBatchLoader;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTaskNameParams;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseQueryBuilder;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseSubEntityQueryBuilder;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseSubEntityResultParser;
import com.hotpads.datarouter.client.type.HBaseClient;
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
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.exception.NotImplementedException;
import com.hotpads.util.core.iterable.PeekableIterable;
import com.hotpads.util.core.iterable.scanner.batch.BatchLoader;
import com.hotpads.util.core.iterable.scanner.batch.BatchingSortedScanner;
import com.hotpads.util.core.iterable.scanner.imp.ListBackedSortedScanner;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class HBaseSubEntityReaderNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BasePhysicalNode<PK,D,F>
implements HBasePhysicalNode<PK,D>,
		SubEntitySortedMapStorageReaderNode<EK,PK,D,F>
{
	protected Logger logger = Logger.getLogger(getClass());
	
	public static final int DEFAULT_ITERATE_BATCH_SIZE = HBaseReaderNode.DEFAULT_ITERATE_BATCH_SIZE;

	private HBaseTaskNameParams taskNameParams;
	protected EntityFieldInfo<EK,E> entityFieldInfo;
	
	protected HBaseSubEntityQueryBuilder<EK,E,PK,D,F> queryBuilder;
	protected HBaseSubEntityResultParser<EK,PK,D,F> resultParser;
	
	/******************************* constructors ************************************/
	
	public HBaseSubEntityReaderNode(EntityNodeParams<EK,E> entityNodeParams, NodeParams<PK,D,F> params){
		super(params);
		this.taskNameParams = new HBaseTaskNameParams(getClientName(), getTableName(), getName());
		this.entityFieldInfo = new EntityFieldInfo<>(entityNodeParams);
		this.queryBuilder = new HBaseSubEntityQueryBuilder<EK,E,PK,D,F>(entityFieldInfo, fieldInfo);
		this.resultParser = new HBaseSubEntityResultParser<EK,PK,D,F>(fieldInfo);
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public HBaseClient getClient(){
		return (HBaseClient)getRouter().getClient(getClientName());
	}
	
	@Override
	public String getEntityNodePrefix(){
		return fieldInfo.getEntityNodePrefix();
	}
	
	@Override
	public void clearThreadSpecificState(){
	}
	
	
	/************************************ MapStorageReader methods ****************************/
	
	@Override
	public boolean exists(PK key, Config config) {
		//should probably make a getKey method
		return get(key, config) != null;
	}

	
	@Override
	public D get(final PK key, final Config pConfig){
		if(key==null){ return null; }
		final Config config = Config.nullSafe(pConfig);
		return CollectionTool.getFirst(getMulti(ListTool.wrap(key), config));
	}
	
	
	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config pConfig){	
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<D>(); }
		final Config config = Config.nullSafe(pConfig);
		return new HBaseMultiAttemptTask<List<D>>(new HBaseTask<List<D>>(getDataRouterContext(), getTaskNameParams(), "getMulti", config){
				public List<D> hbaseCall() throws Exception{
					DRCounters.incSuffixClientNode(client.getType(), "getMulti rows", getClientName(), getNodeName(), 
							CollectionTool.size(keys));
					List<Get> gets = ListTool.createArrayListWithSize(keys);
					for(PK pk : keys){
						byte[] rowBytes = queryBuilder.getRowBytesWithPartition(pk.getEntityKey());
						Get get = new Get(rowBytes);
						byte[] qualifierPrefix = queryBuilder.getQualifierPrefix(pk);
						get.setFilter(new ColumnPrefixFilter(qualifierPrefix));
						gets.add(get);
					}
					Result[] rows = hTable.get(gets);
					return resultParser.getDatabeansWithMatchingQualifierPrefix(rows);
				}
			}).call();
	}
	
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config pConfig) {	
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<PK>(); }
		final Config config = Config.nullSafe(pConfig);
		return new HBaseMultiAttemptTask<List<PK>>(new HBaseTask<List<PK>>(getDataRouterContext(), getTaskNameParams(), "getKeys", config){
				public List<PK> hbaseCall() throws Exception{
					DRCounters.incSuffixClientNode(client.getType(), "getKeys rows", getClientName(), getNodeName(), 
							CollectionTool.size(keys));
					List<Get> gets = ListTool.createArrayListWithSize(keys);
					for(PK pk : keys){
						byte[] rowBytes = queryBuilder.getRowBytesWithPartition(pk.getEntityKey());
						byte[] qualifierPrefix = queryBuilder.getQualifierPrefix(pk);
						FilterList filters = new FilterList();
						filters.addFilter(new KeyOnlyFilter());
						filters.addFilter(new ColumnPrefixFilter(qualifierPrefix));
						Get get = new Get(rowBytes);
						get.setFilter(filters);
						gets.add(get);
					}
					Result[] hBaseResults = hTable.get(gets);
					List<PK> results = ListTool.createArrayList();
					for(Result row : hBaseResults){
						if(row.isEmpty()){ continue; }
						NavigableSet<PK> pksFromSingleGet = resultParser.getPrimaryKeysWithMatchingQualifierPrefix(row);
						results.addAll(CollectionTool.nullSafe(pksFromSingleGet));
					}
					return results;
				}
			}).call();
	}

	
	/************************* sorted **********************************/
	
	@Override
	public PK getFirstKey(Config pConfig){
		Config config = Config.nullSafe(pConfig).setLimit(1);
		return CollectionTool.getFirst(
				getKeysInRange(null, true, null, true, config));
	}

	
	@Override
	public D getFirst(Config pConfig){
		Config config = Config.nullSafe(pConfig).setLimit(1);
		return CollectionTool.getFirst(
				getRange(null, true, null, true, config));
	}
	
	
	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		return getWithPrefixes(ListTool.wrap(prefix), wildcardLastField, config);
	}

	
	@Override
	public List<D> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField, 
			final Config pConfig){
		if(CollectionTool.isEmpty(prefixes)){ return new LinkedList<D>(); }
		final Config config = Config.nullSafe(pConfig);

		//segment prefixes into single vs multi-row queries
		final List<PK> singleEntityPrefixes = ListTool.createArrayList();
		final List<PK> multiEntityPrefixes = ListTool.createArrayList();
		for(PK prefix : prefixes){
			if(queryBuilder.isSingleEkPrefixQuery(prefix, wildcardLastField)){
				singleEntityPrefixes.add(prefix);
			}else{
				multiEntityPrefixes.add(prefix);
			}
		}
		
		//execute the single-row queries in a big multi-Get
		List<D> singleEntityResults = new HBaseMultiAttemptTask<List<D>>(new HBaseTask<List<D>>(
				getDataRouterContext(), getTaskNameParams(), "getWithPrefixes", config){
				public List<D> hbaseCall() throws Exception{
					List<Get> gets = queryBuilder.getPrefixGets(singleEntityPrefixes, wildcardLastField, config);
					Result[] hbaseRows = hTable.get(gets);
					return resultParser.getDatabeansWithMatchingQualifierPrefix(hbaseRows);
				}
			}).call();

		//execute the multi-row queries in individual Scans
		List<D> multiEntityResults = ListTool.createArrayList();
		for(final PK prefix : multiEntityPrefixes){		
			final Scan scan = queryBuilder.getPrefixScan(prefix, wildcardLastField, config);
			List<D> singleScanResults = new HBaseMultiAttemptTask<List<D>>(new HBaseTask<List<D>>(
					getDataRouterContext(), getTaskNameParams(), "getWithPrefixes", config){
					public List<D> hbaseCall() throws Exception{
						List<D> results = ListTool.createArrayList();
						managedResultScanner = hTable.getScanner(scan);
						for(Result row : managedResultScanner){
							if(row.isEmpty()){ continue; }
							List<D> singleRowResults = resultParser.getDatabeansWithMatchingQualifierPrefix(row);
							results.addAll(singleRowResults);
							if(config.getLimit()!=null && results.size()>=config.getLimit()){ break; }
						}
						return results;
					}
				}).call();
			multiEntityResults.addAll(singleScanResults);
		}
		
		return ListTool.concatenate(singleEntityResults, multiEntityResults);
	}
	

	@Deprecated
	@Override
	public List<PK> getKeysInRange(final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		PeekableIterable<PK> iter = scanKeys(Range.create(start, startInclusive, end, endInclusive), pConfig);
		int limit = config.getLimitOrUse(Integer.MAX_VALUE);
		List<PK> results = IterableTool.createArrayListFromIterable(iter, limit);
		return results;
	}
	

	@Deprecated
	@Override
	public List<D> getRange(final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		PeekableIterable<D> iter = scan(Range.create(start, startInclusive, end, endInclusive), pConfig);
		int limit = config.getLimitOrUse(Integer.MAX_VALUE);
		List<D> results = IterableTool.createArrayListFromIterable(iter, limit);
		return results;
		
	}

	@Deprecated
	@Override
	public List<D> getPrefixedRange(final PK prefix, final boolean wildcardLastField, 
			final PK start, final boolean startInclusive, 
			final Config pConfig){
		throw new NotImplementedException("apologies");
	}
	
	@Override
	public SortedScannerIterable<PK> scanKeys(final Range<PK> pRange, final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		final Range<PK> range = Range.nullSafe(pRange);
		final Range<EK> ekRange = queryBuilder.getEkRange(range);
		if(ekRange.hasStart() && ekRange.equalsStartEnd()){//single row.  use Get.  gets all pks in entity.  no way to limit rows
			List<PK> pks = new HBaseMultiAttemptTask<List<PK>>(new HBaseTask<List<PK>>(getDataRouterContext(), getTaskNameParams(), "scanPksInEntity", config){
				public List<PK> hbaseCall() throws Exception{
					Get get = queryBuilder.getSingleRowRange(range.getStart().getEntityKey(), range, true);
					Result result = hTable.get(get);
					return ListTool.createArrayList(resultParser.getPrimaryKeysWithMatchingQualifierPrefix(
							result));	
				}}).call();
			return new SortedScannerIterable<PK>(new ListBackedSortedScanner<PK>(pks));
		}else{
			BatchLoader<PK> firstBatchLoader = new HBaseEntityPrimaryKeyBatchLoader<EK,E,PK,D,F>(this, range, pConfig, 1L);//start the counter at 1
			BatchingSortedScanner<PK> scanner = new BatchingSortedScanner<PK>(getClient().getExecutorService(), firstBatchLoader);
			return new SortedScannerIterable<PK>(scanner);
		}
	}
	
	@Override
	public SortedScannerIterable<D> scan(final Range<PK> pRange, final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		final Range<PK> range = Range.nullSafe(pRange);
		final Range<EK> ekRange = queryBuilder.getEkRange(range);
		if(ekRange.hasStart() && ekRange.equalsStartEnd()){//single row.  use Get.  gets all databeans in entity.  no way to limit rows
			List<D> databeans = new HBaseMultiAttemptTask<List<D>>(new HBaseTask<List<D>>(getDataRouterContext(), getTaskNameParams(), "scanInEntity", config){
				public List<D> hbaseCall() throws Exception{
					Get get = queryBuilder.getSingleRowRange(range.getStart().getEntityKey(), range, false);
					Result result = hTable.get(get);
					return resultParser.getDatabeansWithMatchingQualifierPrefix(
							result);	
				}}).call();
			return new SortedScannerIterable<D>(new ListBackedSortedScanner<D>(databeans));
		}else{
			BatchLoader<D> firstBatchLoader = new HBaseEntityDatabeanBatchLoader<EK,E,PK,D,F>(this, range, pConfig, 1L);//start the counter at 1
			BatchingSortedScanner<D> scanner = new BatchingSortedScanner<D>(getClient().getExecutorService(), firstBatchLoader);
			return new SortedScannerIterable<D>(scanner);
		}
	}
		
	
	/***************************** helper methods **********************************/
	
	/*
	 * internal method to fetch a single batch of hbase rows/keys.  only public so that iterators in other packages
	 * can use it
	 * 
	 * warning: we cannot currently limit the number of databeans/pks, only hbase rows.  be aware that it will probably
	 * return more databeans/pks than iterateBatchSize
	 */
	public List<Result> getResultsInSubRange(final Range<PK> rowRange, final boolean keysOnly, final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		final String scanKeysVsRowsNumBatches = "scan " + (keysOnly ? "key" : "row") + " numBatches";
		final String scanKeysVsRowsNumRows = "scan " + (keysOnly ? "key" : "row") + " numRows";
//		final String scanKeysVsRowsNumCells = "scan " + (keysOnly ? "key" : "row") + " numCells";//need a clean way to get cell count
		return new HBaseMultiAttemptTask<List<Result>>(new HBaseTask<List<Result>>(getDataRouterContext(), getTaskNameParams(), scanKeysVsRowsNumBatches,
				config){
				public List<Result> hbaseCall() throws Exception{
					Range<ByteRange> rowBytesRange = queryBuilder.getRowRange(rowRange);
					Scan scan = HBaseQueryBuilder.getScanForRange(rowBytesRange, config);
					FilterList filterList = new FilterList();
					if(keysOnly){ filterList.addFilter(new KeyOnlyFilter()); }
					filterList.addFilter(new ColumnPrefixFilter(fieldInfo.getEntityColumnPrefixBytes()));
					scan.setFilter(filterList);
					managedResultScanner = hTable.getScanner(scan);
					List<Result> results = ListTool.createArrayList();
					for(Result row : managedResultScanner){
						if(row.isEmpty()){ continue; }
						results.add(row);
						if(config.getIterateBatchSize()!=null && results.size()>=config.getIterateBatchSize()){ break; }
						if(config.getLimit()!=null && results.size()>=config.getLimit()){ break; }
					}
					managedResultScanner.close();
					DRCounters.incSuffixClientNode(client.getType(), scanKeysVsRowsNumRows, getClientName(), getNodeName(),  
							CollectionTool.size(results));
					return results;
				}
			}).call();
	}
	
	
	/********************* get/set *******************************/

	public HBaseSubEntityResultParser<EK,PK,D,F> getResultParser(){
		return resultParser;
	}


	public HBaseTaskNameParams getTaskNameParams(){
		return taskNameParams;
	}	
	
	
	
}
