package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.scan.HBaseDatabeanScanner;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTaskNameParams;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseQueryBuilder;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseScatteringPrefixQueryBuilder;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.PeekableIterable;
import com.hotpads.util.core.iterable.scanner.batch.BatchingSortedScanner;
import com.hotpads.util.core.iterable.scanner.collate.Collator;
import com.hotpads.util.core.iterable.scanner.collate.PriorityQueueCollator;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class HBaseReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BasePhysicalNode<PK,D,F>
implements HBasePhysicalNode<PK,D>,
		MapStorageReader<PK,D>,
		SortedStorageReader<PK,D>
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final int DEFAULT_ITERATE_BATCH_SIZE = 1000;
	
	//for backwards compatibility with a few tables
	protected Boolean primaryKeyHasUnnecessaryTrailingSeparatorByte;
	public static final Set<String> TRAILING_BYTE_TABLES = SetTool.createHashSet();
	static{
		TRAILING_BYTE_TABLES.add("AvailableCounter");
		TRAILING_BYTE_TABLES.add("DocumentView");
		TRAILING_BYTE_TABLES.add("JsonListingView");
		TRAILING_BYTE_TABLES.add("ListingTrait");
		TRAILING_BYTE_TABLES.add("ListingTraitByDate");
		TRAILING_BYTE_TABLES.add("ListingTraitByState");
		TRAILING_BYTE_TABLES.add("ModelIndexListingView");
		TRAILING_BYTE_TABLES.add("ModelIndexListingViewByListingKey");
		TRAILING_BYTE_TABLES.add("MonthlyListingSummary");
	}
	
	private HBaseTaskNameParams taskNameParams;
	
	/******************************* constructors ************************************/
	
	public HBaseReaderNode(NodeParams<PK,D,F> params){
		super(params);
		detectPrimaryKeyHasUnnecessaryTrailingSeparatorByte();
		this.taskNameParams = new HBaseTaskNameParams(getClientName(), getTableName(), getName());
	}
	
	protected void detectPrimaryKeyHasUnnecessaryTrailingSeparatorByte(){
		Assert.assertTrue(StringTool.notEmpty(fieldInfo.getTableName()));
		primaryKeyHasUnnecessaryTrailingSeparatorByte = TRAILING_BYTE_TABLES.contains(fieldInfo.getTableName());
		if(primaryKeyHasUnnecessaryTrailingSeparatorByte){
			logger.warn("primaryKeyHasUnnecessaryTrailingSeparatorByte for table "+fieldInfo.getTableName());
		}
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
		return new HBaseMultiAttemptTask<D>(new HBaseTask<D>(getDataRouterContext(), getTaskNameParams(), "get", config){
				public D hbaseCall() throws Exception{
					byte[] rowBytes = getKeyBytesWithScatteringPrefix(null, key, false);
					Result row = hTable.get(new Get(rowBytes));
					if(row.isEmpty()){ return null; }
					D result = HBaseResultTool.getDatabean(row, fieldInfo);
					return result;
				}
			}).call();
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
					for(PK key : keys){
						byte[] rowBytes = getKeyBytesWithScatteringPrefix(null, key, false);
						gets.add(new Get(rowBytes));
					}
					Result[] resultArray = hTable.get(gets);
					return HBaseResultTool.getDatabeans(Arrays.asList(resultArray), fieldInfo);
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
					for(PK key : keys){
						byte[] rowBytes = getKeyBytesWithScatteringPrefix(null, key, false);
						Get get = new Get(rowBytes);
						//FirstKeyOnlyFilter returns value too, so it's better if value in each row is not large
						get.setFilter(new FirstKeyOnlyFilter());
						gets.add(get);
					}
					Result[] resultArray = hTable.get(gets);
					return HBaseResultTool.getPrimaryKeys(Arrays.asList(resultArray), fieldInfo);
				}
			}).call();
	}

	
	/******************************* Sorted *************************************/
	
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
		final List<D> results = ListTool.createArrayList();
		List<Scan> scanForEachScatteringPartition = HBaseScatteringPrefixQueryBuilder.getPrefixScanners(fieldInfo, 
				prefixes, wildcardLastField, config);
		for(final Scan scan : scanForEachScatteringPartition){
			new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>(getDataRouterContext(), getTaskNameParams(), "getWithPrefixes", config){
					public Void hbaseCall() throws Exception{
						managedResultScanner = hTable.getScanner(scan);
						for(Result row : managedResultScanner){
							if(row.isEmpty()){ continue; }
							D result = HBaseResultTool.getDatabean(row, fieldInfo);
							results.add(result);//add results directly to the parent result list
							//TODO terribly inneficient limiting.  fetches full limit for every scattering partition
							if(config.getLimit()!=null && results.size()>=config.getLimit()){ break; }
						}
						return null;
					}
				}).call();
		}
		sortIfScatteringPrefixExists(results);
		return results;
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
		Collator<PK> collator = new PriorityQueueCollator<PK>(scanners);
		return new SortedScannerIterable<PK>(collator);
	}
	
	@Override
	public SortedScannerIterable<D> scan(final Range<PK> pRange, final Config pConfig){
		Range<PK> range = Range.nullSafe(pRange);
		List<BatchingSortedScanner<D>> scanners = HBaseScatteringPrefixQueryBuilder
				.getBatchingDatabeanScannerForEachPrefix(getClient().getExecutorService(), this, fieldInfo, range,
						pConfig);
		Collator<D> collator = new PriorityQueueCollator<D>(scanners);
		return new SortedScannerIterable<D>(collator);
	}
		
	
	/***************************** helper methods **********************************/
	
	/*
	 * internal method to fetch a single batch of hbase rows/keys.  only public so that iterators in other packages
	 * can use it
	 */
	public List<Result> getResultsInSubRange(final Range<ByteRange> range, final boolean keysOnly, final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		final String scanKeysVsRowsNumBatches = "scan " + (keysOnly ? "key" : "row") + " numBatches";
		final String scanKeysVsRowsNumRows = "scan " + (keysOnly ? "key" : "row") + " numRows";
//		final String scanKeysVsRowsNumCells = "scan " + (keysOnly ? "key" : "row") + " numCells";//need a clean way to get cell count
		return new HBaseMultiAttemptTask<List<Result>>(new HBaseTask<List<Result>>(getDataRouterContext(), getTaskNameParams(), scanKeysVsRowsNumBatches,
				config){
				public List<Result> hbaseCall() throws Exception{
					ByteRange start = range.getStart();
					if(start!=null && !range.getStartInclusive()){//careful: this may have already been set by scatteringPrefix logic
						start = new ByteRange(ByteTool.unsignedIncrement(start.toArray()));
					}
					ByteRange end = range.getEnd();
					
					//startInclusive already adjusted for
					Range<ByteRange> scanRange = Range.create(start, true, end, range.getEndInclusive());
					Scan scan = HBaseQueryBuilder.getScanForRange(scanRange, config);
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
					DRCounters.incSuffixClientNode(client.getType(), scanKeysVsRowsNumRows, getClientName(), getNodeName(),  
							CollectionTool.size(results));
					return results;
				}
			}).call();
	}
	
	//this method is in the node because it deals with the messy primaryKeyHasUnnecessaryTrailingSeparatorByte
	public byte[] getKeyBytesWithScatteringPrefix(List<Field<?>> overrideScatteringPrefixFields, PK key, boolean increment){
		//return only scatteringPrefix bytes
		if(key==null){
			if(CollectionTool.isEmpty(overrideScatteringPrefixFields)){
				return new byte[]{};
			}
			return FieldSetTool.getConcatenatedValueBytes(overrideScatteringPrefixFields, false, false);
		}
		
		//else return scatteringPrefix bytes + keyBytes + (maybe) trailing separator
		List<Field<?>> scatteringPrefixFields = ListTool.createLinkedList();
		if(CollectionTool.notEmpty(overrideScatteringPrefixFields)){
			scatteringPrefixFields.addAll(overrideScatteringPrefixFields);
		}else{
			//maybe Assert the override fields match those returned for the key
			scatteringPrefixFields.addAll(fieldInfo.getSampleScatteringPrefix().getScatteringPrefixFields(key));
		}
		byte[] scatteringPrefixBytes = FieldSetTool.getConcatenatedValueBytes(scatteringPrefixFields, true, false);
		byte[] keyBytes = FieldSetTool.getConcatenatedValueBytes(key.getFields(), true, false);
		if(increment){
			keyBytes = ByteTool.unsignedIncrement(keyBytes);
		}
		byte[] bytes;
		if(primaryKeyHasUnnecessaryTrailingSeparatorByte){
			bytes = ByteTool.concatenate(scatteringPrefixBytes, keyBytes, new byte[]{StringField.SEPARATOR});
		}else{
			bytes = ByteTool.concatenate(scatteringPrefixBytes, keyBytes);
		}
		return bytes;
	}
	
	private <T extends Comparable<? super T>> void sortIfScatteringPrefixExists(List<T> ins){
		if(fieldInfo.getSampleScatteringPrefix().getNumPrefixBytes() > 0){
			Collections.sort(ins);
		}
	}

	public Boolean getPrimaryKeyHasUnnecessaryTrailingSeparatorByte(){
		return primaryKeyHasUnnecessaryTrailingSeparatorByte;
	}
	
	public HBaseTaskNameParams getTaskNameParams(){
		return taskNameParams;
	}
	
	
}
