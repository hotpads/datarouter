package com.hotpads.datarouter.client.imp.hbase.node;

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
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.scan.HBaseDatabeanScanner;
import com.hotpads.datarouter.client.imp.hbase.scan.HBasePrimaryKeyScanner;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseQueryBuilder;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseScatteringPrefixQueryBuilder;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.iterable.PeekableIterable;
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
	protected Logger logger = Logger.getLogger(getClass());
	
	public static final int DEFAULT_ITERATE_BATCH_SIZE = 1000;
	
	//for backwards compatibility with a few tables
	protected Boolean primaryKeyHasUnnecessaryTrailingSeparatorByte;
	public static final Set<String> TRAILING_BYTE_TABLES = SetTool.createHashSet();
	static{
		TRAILING_BYTE_TABLES.add("AvailableCounter");
		TRAILING_BYTE_TABLES.add("DocumentView");
		TRAILING_BYTE_TABLES.add("JsonListingView");
		TRAILING_BYTE_TABLES.add("KeepAlive");
		TRAILING_BYTE_TABLES.add("ListingTrait");
		TRAILING_BYTE_TABLES.add("ListingTraitByDate");
		TRAILING_BYTE_TABLES.add("ListingTraitByState");
		TRAILING_BYTE_TABLES.add("ModelIndexListingView");
		TRAILING_BYTE_TABLES.add("ModelIndexListingViewByListingKey");
		TRAILING_BYTE_TABLES.add("MonthlyListingSummary");
	}
	
	/******************************* constructors ************************************/

	public HBaseReaderNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, fielderClass, router, clientName, physicalName, qualifiedPhysicalName);
		detectPrimaryKeyHasUnnecessaryTrailingSeparatorByte();
	}
	
	public HBaseReaderNode(Class<D> databeanClass,Class<F> fielderClass,
			DataRouter router, String clientName) {
		super(databeanClass, fielderClass, router, clientName);
		detectPrimaryKeyHasUnnecessaryTrailingSeparatorByte();
	}

	public HBaseReaderNode(Class<? super D> baseDatabeanClass, Class<D> databeanClass, 
			Class<F> fielderClass, DataRouter router, String clientName){
		super(baseDatabeanClass, databeanClass, fielderClass, router, clientName);
		detectPrimaryKeyHasUnnecessaryTrailingSeparatorByte();
	}
	
	protected void detectPrimaryKeyHasUnnecessaryTrailingSeparatorByte(){
		Assert.assertTrue(StringTool.notEmpty(tableName));
		primaryKeyHasUnnecessaryTrailingSeparatorByte = TRAILING_BYTE_TABLES.contains(tableName);
		if(primaryKeyHasUnnecessaryTrailingSeparatorByte){
			logger.warn("primaryKeyHasUnnecessaryTrailingSeparatorByte for table "+tableName);
		}
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public HBaseClient getClient(){
		return (HBaseClient)this.router.getClient(getClientName());
	}
	
	@Override
	public void clearThreadSpecificState(){
	}

//	public HTable checkOutHTable(){
//		return this.getClient().checkOutHTable(this.getTableName());
//	}
//	
//	public void checkInHTable(HTable hTable){
//		this.getClient().checkInHTable(hTable);
//	}
	
	
	/************************************ MapStorageReader methods ****************************/
	
	@Override
	public boolean exists(PK key, Config config) {
		return get(key, config) != null;
	}

	
	@Override
	public D get(final PK key, final Config pConfig){
		if(key==null){ return null; }
		final Config config = Config.nullSafe(pConfig);
		return new HBaseMultiAttemptTask<D>(new HBaseTask<D>("get", this, config){
				public D hbaseCall() throws Exception{
					byte[] rowBytes = getKeyBytesWithScatteringPrefix(key);
					Result row = hTable.get(new Get(rowBytes));
					if(row.isEmpty()){ return null; }
					D result = HBaseResultTool.getDatabean(row, fieldInfo);
					return result;
				}
			}).call();
	}
	
	
	@Override
	public List<D> getAll(final Config pConfig){
		return getRange(null, true, null, true, pConfig);
	}

	
	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config pConfig){	
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<D>(); }
		final Config config = Config.nullSafe(pConfig);
		return new HBaseMultiAttemptTask<List<D>>(new HBaseTask<List<D>>("getMulti", this, config){
				public List<D> hbaseCall() throws Exception{
//					DRCounters.inc(node.getName()+" hbase getMulti rows", CollectionTool.size(keys));
					DRCounters.incPrefixClientNode("hbase getMulti rows", clientName, node.getName());
					List<Get> gets = ListTool.createArrayListWithSize(keys);
					for(PK key : keys){
						byte[] rowBytes = getKeyBytesWithScatteringPrefix(key);
						gets.add(new Get(rowBytes));
					}
					Result[] resultArray = hTable.get(gets);
					List<D> results = ListTool.createArrayListWithSize(keys);
					for(Result row : resultArray){
						if(row==null || row.isEmpty()){ continue; }
						D result = HBaseResultTool.getDatabean(row, fieldInfo);
						results.add(result);
					}
					return results;
				}
			}).call();
	}
	
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config pConfig) {	
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<PK>(); }
		final Config config = Config.nullSafe(pConfig);
		return new HBaseMultiAttemptTask<List<PK>>(new HBaseTask<List<PK>>("getKeys", this, config){
				public List<PK> hbaseCall() throws Exception{
//					DRCounters.inc(node.getName()+" hbase getKeys rows", CollectionTool.size(keys));
					DRCounters.incPrefixClientNode("hbase getKeys rows", clientName, node.getName());
					List<Get> gets = ListTool.createArrayListWithSize(keys);
					for(PK key : keys){
						byte[] rowBytes = getKeyBytesWithScatteringPrefix(key);
						Get get = new Get(rowBytes);
						get.setFilter(new FirstKeyOnlyFilter());//make sure first column in row is not something big
						gets.add(get);
					}
					Result[] resultArray = hTable.get(gets);
					List<PK> results = ListTool.createArrayListWithSize(keys);
					for(Result row : resultArray){
						if(row==null || row.isEmpty()){ continue; }
						PK result = HBaseResultTool.getPrimaryKey(row.getRow(), fieldInfo);
						results.add(result);
					}
					return results;
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
	public List<D> getWithPrefixes(final Collection<? extends PK> prefixes, 
			final boolean wildcardLastField, final Config pConfig){
		if(CollectionTool.isEmpty(prefixes)){ return new LinkedList<D>(); }
		final Config config = Config.nullSafe(pConfig);
		final List<D> results = ListTool.createArrayList();
		List<Scan> scanForEachScatteringPartition = HBaseScatteringPrefixQueryBuilder
				.getPrefixScanners(fieldInfo, prefixes, wildcardLastField, config);
		for(final Scan scan : scanForEachScatteringPartition){
			new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>("getWithPrefixes", this, config){
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
	

	@Override
	public List<PK> getKeysInRange(final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		PeekableIterable<PK> iter = scanKeys(start, startInclusive, end, endInclusive, pConfig);
		int limit = config.getLimitOrUse(Integer.MAX_VALUE);
		List<PK> results = IterableTool.createArrayListFromIterable(iter, limit);
		return results;
	}
	

	@Override
	public List<D> getRange(final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		PeekableIterable<D> iter = scan(start, startInclusive, end, endInclusive, pConfig);
		int limit = config.getLimitOrUse(Integer.MAX_VALUE);
		List<D> results = IterableTool.createArrayListFromIterable(iter, limit);
		return results;
		
	}

	@Override
	public List<D> getPrefixedRange(
			final PK prefix, final boolean wildcardLastField, 
			final PK start, final boolean startInclusive, 
			final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		List<Pair<byte[],byte[]>> prefixedRanges = HBaseScatteringPrefixQueryBuilder.getPrefixedRanges(fieldInfo,  
				prefix, wildcardLastField, start, startInclusive, null, true, config);
		List<HBaseDatabeanScanner<PK,D>> scanners = HBaseScatteringPrefixQueryBuilder.getManualDatabeanScannersForRanges(
				this, fieldInfo, prefixedRanges, pConfig);
		Collator<D> collator = new PriorityQueueCollator<D>(scanners);
		Iterable<D> iterable = new SortedScannerIterable<D>(collator);
		int limit = config.getLimitOrUse(Integer.MAX_VALUE);
		List<D> results = IterableTool.createArrayListFromIterable(iterable, limit);
		return results;
	}

	
	@Override
	public PeekableIterable<PK> scanKeys(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		List<HBasePrimaryKeyScanner<PK,D>> scanners = HBaseScatteringPrefixQueryBuilder.getManualPrimaryKeyScannerForEachPrefix(
				this, fieldInfo, start, startInclusive, end, endInclusive, config);
		Collator<PK> collator = new PriorityQueueCollator<PK>(scanners);
		return new SortedScannerIterable<PK>(collator);
	}

	
	@Override
	public PeekableIterable<D> scan(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		List<HBaseDatabeanScanner<PK,D>> scanners = HBaseScatteringPrefixQueryBuilder.getManualDatabeanScannerForEachPrefix(
				this, fieldInfo, start, startInclusive, end, endInclusive, config);
		Collator<D> collator = new PriorityQueueCollator<D>(scanners);
		return new SortedScannerIterable<D>(collator);
	}
		
	
	/************************ helpers ********************************/

	public List<Result> getResultsInSubRange(final byte[] start, final boolean startInclusive, final byte[] end, 
			final boolean keysOnly, final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		return new HBaseMultiAttemptTask<List<Result>>(new HBaseTask<List<Result>>("getResultsInSubRange", this, config){
				public List<Result> hbaseCall() throws Exception{
					Scan scan = HBaseQueryBuilder.getScanForRange(start, startInclusive, end, config);
					if(keysOnly){ scan.setFilter(new FirstKeyOnlyFilter()); }
					managedResultScanner = hTable.getScanner(scan);
					List<Result> results = ListTool.createArrayList();
					for(Result rowKey : managedResultScanner){
						if(rowKey.isEmpty()){ continue; }
						results.add(rowKey);
						if(config.getIterateBatchSize()!=null && results.size()>=config.getIterateBatchSize()){ break; }
						if(config.getLimit()!=null && results.size()>=config.getLimit()){ break; }
					}
					managedResultScanner.close();
					return results;
				}
			}).call();
	}
	
	protected byte[] getKeyBytesWithScatteringPrefix(PK key){
		List<Field<?>> keyPlusScatteringPrefixFields = fieldInfo.getKeyFieldsWithScatteringPrefix(key);
		byte[] bytes = FieldSetTool.getConcatenatedValueBytes(keyPlusScatteringPrefixFields, false,
				primaryKeyHasUnnecessaryTrailingSeparatorByte);
		return bytes;
	}
	
	protected <T extends Comparable<? super T>> void sortIfScatteringPrefixExists(List<T> ins){
		if(fieldInfo.getSampleScatteringPrefix().getNumPrefixBytes() > 0){
			Collections.sort(ins);
		}
	}
	
}
