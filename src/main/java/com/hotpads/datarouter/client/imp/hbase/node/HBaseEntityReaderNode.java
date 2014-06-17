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
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.batching.HBaseDatabeanBatchLoader;
import com.hotpads.datarouter.client.imp.hbase.scan.HBaseDatabeanScanner;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseEntityQueryBuilder;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseEntityResultTool;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseQueryBuilder;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseScatteringPrefixQueryBuilder;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.PeekableIterable;
import com.hotpads.util.core.iterable.scanner.batch.BatchLoader;
import com.hotpads.util.core.iterable.scanner.batch.BatchingSortedScanner;
import com.hotpads.util.core.iterable.scanner.collate.Collator;
import com.hotpads.util.core.iterable.scanner.collate.PriorityQueueCollator;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class HBaseEntityReaderNode<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BasePhysicalNode<PK,D,F>
implements HBasePhysicalNode<PK,D>,
		SortedMapStorageReader<PK,D>
{
	protected Logger logger = Logger.getLogger(getClass());
	
	public static final int DEFAULT_ITERATE_BATCH_SIZE = HBaseReaderNode.DEFAULT_ITERATE_BATCH_SIZE;
	
	protected byte[] columnPrefixBytes;
	
	/******************************* constructors ************************************/
	
	public HBaseEntityReaderNode(NodeParams<PK,D,F> params){
		super(params);
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public HBaseClient getClient(){
		return (HBaseClient)getRouter().getClient(getClientName());
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
		return new HBaseMultiAttemptTask<List<D>>(new HBaseTask<List<D>>(getDataRouterContext(), "getMulti", this, config){
				public List<D> hbaseCall() throws Exception{
					DRCounters.incSuffixClientNode(client.getType(), "getMulti rows", getClientName(), node.getName(), 
							CollectionTool.size(keys));
					List<Get> gets = ListTool.createArrayListWithSize(keys);
					for(PK key : keys){
						byte[] rowBytes = getRowBytes(key.getEntityKey());
						Get get = new Get(rowBytes);
						byte[] qualifierPkBytes = getQualifierPkBytes(key);
						byte[] qualifierPrefix = ByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(), 
								qualifierPkBytes);
						get.setFilter(new ColumnPrefixFilter(qualifierPrefix));
						gets.add(get);
					}
					Result[] rows = hTable.get(gets);
					return new HBaseEntityResultTool<EK,PK,D,F>(fieldInfo).getDatabeansWithMatchingQualifierPrefix(rows);
				}
			}).call();
	}
	
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config pConfig) {	
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<PK>(); }
		final Config config = Config.nullSafe(pConfig);
		return new HBaseMultiAttemptTask<List<PK>>(new HBaseTask<List<PK>>(getDataRouterContext(), "getKeys", this, config){
				public List<PK> hbaseCall() throws Exception{
					DRCounters.incSuffixClientNode(client.getType(), "getKeys rows", getClientName(), node.getName(), 
							CollectionTool.size(keys));
					List<Get> gets = ListTool.createArrayListWithSize(keys);
					for(PK key : keys){
						byte[] rowBytes = getRowBytes(key.getEntityKey());
						byte[] qualifierPkBytes = getQualifierPkBytes(key);
						byte[] qualifierPrefix = ByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(), 
								qualifierPkBytes);
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
						NavigableSet<PK> pksFromSingleGet = new HBaseEntityResultTool<EK,PK,D,F>(fieldInfo)
								.getPrimaryKeysWithMatchingQualifierPrefix(row);
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
		if(wildcardLastField){
			throw new IllegalArgumentException("currently cannot wildcardLastField in HBaseEntityNode");
		}
		final Config config = Config.nullSafe(pConfig);
		return new HBaseMultiAttemptTask<List<D>>(new HBaseTask<List<D>>(getDataRouterContext(), "getWithPrefixes", this, config){
				public List<D> hbaseCall() throws Exception{
					List<Get> gets = new HBaseEntityQueryBuilder<EK,PK,D,F>(fieldInfo).getPrefixQueries(prefixes, config);
					Result[] hbaseRows = hTable.get(gets);
					return new HBaseEntityResultTool<EK,PK,D,F>(fieldInfo).getDatabeansWithMatchingQualifierPrefix(hbaseRows);
				}
			}).call();
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

	@Override
	public List<D> getPrefixedRange(final PK prefix, final boolean wildcardLastField, 
			final PK start, final boolean startInclusive, 
			final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		List<Pair<byte[],byte[]>> prefixedRanges = HBaseScatteringPrefixQueryBuilder.getPrefixedRanges(fieldInfo,  
				prefix, wildcardLastField, start, startInclusive, null, true, config);
		List<HBaseDatabeanScanner<PK,D>> scanners = HBaseScatteringPrefixQueryBuilder
				.getManualDatabeanScannersForRanges(this, fieldInfo, prefixedRanges, pConfig);
		Collator<D> collator = new PriorityQueueCollator<D>(scanners);
		Iterable<D> iterable = new SortedScannerIterable<D>(collator);
		int limit = config.getLimitOrUse(Integer.MAX_VALUE);
		List<D> results = IterableTool.createArrayListFromIterable(iterable, limit);
		return results;
	}
	
	@Override
	public SortedScannerIterable<PK> scanKeys(final Range<PK> pRange, final Config pConfig){
		Range<PK> range = Range.nullSafe(pRange);
		List<BatchingSortedScanner<PK>> scanners = HBaseScatteringPrefixQueryBuilder
				.getBatchingPrimaryKeyScannerForEachPrefix(getClient().getExecutorService(), this, fieldInfo, range,
						pConfig);
		//TODO can omit the collator if only one scanner
		Collator<PK> collator = new PriorityQueueCollator<PK>(scanners);
		return new SortedScannerIterable<PK>(collator);
	}
	
	@Override
	public SortedScannerIterable<D> scan(final Range<PK> pRange, final Config pConfig){
		Range<PK> range = Range.nullSafe(pRange);
		BatchLoader<D> firstBatchLoader = new HBaseDatabeanBatchLoader<PK,D,F>(this, scatteringPrefix, 
				range, pConfig, 1L);//start the counter at 1
		BatchingSortedScanner<D> scanner = new BatchingSortedScanner<D>(getClient().getExecutorService(), firstBatchLoader);
		return new SortedScannerIterable<D>(collator);
	}
		
	
	/***************************** helper methods **********************************/
	
	public byte[] getRowBytes(EK entityKey){
		if(entityKey==null){ return new byte[]{}; }
		return FieldTool.getConcatenatedValueBytes(entityKey.getFields(), true, false);
	}
	
	public byte[] getQualifierPkBytes(PK primaryKey){
		if(primaryKey==null){ return new byte[]{}; }
		return FieldTool.getConcatenatedValueBytes(primaryKey.getPostEntityKeyFields(), true, true);
	}
	
	/*
	 * internal method to fetch a single batch of hbase rows/keys.  only public so that iterators in other packages
	 * can use it
	 */
	public List<Result> getResultsInSubRange(final Range<ByteRange> rowRange, final Range<ByteRange> qualifierRange, 
			final boolean keysOnly, final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		final String scanKeysVsRowsNumBatches = "scan " + (keysOnly ? "key" : "row") + " numBatches";
		final String scanKeysVsRowsNumRows = "scan " + (keysOnly ? "key" : "row") + " numRows";
//		final String scanKeysVsRowsNumCells = "scan " + (keysOnly ? "key" : "row") + " numCells";//need a clean way to get cell count
		return new HBaseMultiAttemptTask<List<Result>>(new HBaseTask<List<Result>>(getDataRouterContext(), scanKeysVsRowsNumBatches,
				this, config){
				public List<Result> hbaseCall() throws Exception{
					byte[] start = rowRange.getStart().copyToNewArray();
					if(start!=null && !rowRange.getStartInclusive()){//careful: this may have already been set by scatteringPrefix logic
						start = ByteTool.unsignedIncrement(start);
					}
					byte[] end = rowRange.getEnd() == null? null : rowRange.getEnd().copyToNewArray();
//					if(end!=null && range.getEndInclusive()){//careful: this may have already been set by scatteringPrefix logic
//						end = ByteTool.unsignedIncrement(end);
//					}
					
					//start/endInclusive already adjusted for
					Scan scan = HBaseQueryBuilder.getScanForRange(start, true, end, rowRange.getEndInclusive(), config);
					if(keysOnly){ scan.setFilter(new FirstKeyOnlyFilter()); }
					managedResultScanner = hTable.getScanner(scan);
					List<Result> results = ListTool.createArrayList();
					for(Result row : managedResultScanner){
						if(row.isEmpty()){ continue; }
						results.add(row);
						if(config.getIterateBatchSize()!=null && results.size()>=config.getIterateBatchSize()){ break; }
						if(config.getLimit()!=null && results.size()>=config.getLimit()){ break; }
					}
					managedResultScanner.close();
					DRCounters.incSuffixClientNode(client.getType(), scanKeysVsRowsNumRows, getClientName(), node.getName(),  
							CollectionTool.size(results));
					return results;
				}
			}).call();
	}
	
}
