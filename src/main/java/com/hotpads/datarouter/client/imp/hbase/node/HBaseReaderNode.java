package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.collections.Range;
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
	
	private HBaseTaskNameParams taskNameParams;
	
	/******************************* constructors ************************************/
	
	public HBaseReaderNode(NodeParams<PK,D,F> params){
		super(params);
		this.taskNameParams = new HBaseTaskNameParams(getClientName(), getTableName(), getName());
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public HBaseClient getClient(){
		return (HBaseClient)getRouter().getClient(getClientName());
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
		return new HBaseMultiAttemptTask<D>(new HBaseTask<D>(getDatarouterContext(), getTaskNameParams(), "get", config){
				public D hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
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
		if(DrCollectionTool.isEmpty(keys)){ return new LinkedList<D>(); }
		final Config config = Config.nullSafe(pConfig);
		return new HBaseMultiAttemptTask<List<D>>(new HBaseTask<List<D>>(getDatarouterContext(), getTaskNameParams(), "getMulti", config){
				public List<D> hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
					List<Get> gets = DrListTool.createArrayListWithSize(keys);
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
		if(DrCollectionTool.isEmpty(keys)){ return new LinkedList<PK>(); }
		final Config config = Config.nullSafe(pConfig);
		return new HBaseMultiAttemptTask<List<PK>>(new HBaseTask<List<PK>>(getDatarouterContext(), getTaskNameParams(), "getKeys", config){
				public List<PK> hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
					List<Get> gets = DrListTool.createArrayListWithSize(keys);
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
		return DrIterableTool.first(scanKeys(null, config));
	}

	
	@Override
	public D getFirst(Config pConfig){
		Config config = Config.nullSafe(pConfig).setLimit(1);
		return DrIterableTool.first(scan(null, config));
	}
	
	
	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		return getWithPrefixes(DrListTool.wrap(prefix), wildcardLastField, config);
	}

	
	@Override
	public List<D> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField, 
			final Config pConfig){
		if(DrCollectionTool.isEmpty(prefixes)){ return new LinkedList<D>(); }
		final Config config = Config.nullSafe(pConfig);
		final List<D> results = DrListTool.createArrayList();
		List<Scan> scanForEachScatteringPartition = HBaseScatteringPrefixQueryBuilder.getPrefixScanners(fieldInfo, 
				prefixes, wildcardLastField, config);
		for(final Scan scan : scanForEachScatteringPartition){
			new HBaseMultiAttemptTask<Void>(new HBaseTask<Void>(getDatarouterContext(), getTaskNameParams(), "getWithPrefixes", config){
					public Void hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
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
		return new HBaseMultiAttemptTask<List<Result>>(new HBaseTask<List<Result>>(getDatarouterContext(), getTaskNameParams(), scanKeysVsRowsNumBatches,
				config){
				public List<Result> hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
					ByteRange start = range.getStart();
					if(start!=null && !range.getStartInclusive()){//careful: this may have already been set by scatteringPrefix logic
						start = new ByteRange(DrByteTool.unsignedIncrement(start.toArray()));
					}
					ByteRange end = range.getEnd();
					
					//startInclusive already adjusted for
					Range<ByteRange> scanRange = Range.create(start, true, end, range.getEndInclusive());
					Scan scan = HBaseQueryBuilder.getScanForRange(scanRange, config);
					if(keysOnly){ scan.setFilter(new FirstKeyOnlyFilter()); }
					managedResultScanner = hTable.getScanner(scan);
					List<Result> results = DrListTool.createArrayList();
					for(Result row : managedResultScanner){
						if(row.isEmpty()){ continue; }
						results.add(row);
						if(config.getIterateBatchSize()!=null && results.size()>=config.getIterateBatchSize()){ break; }
						if(config.getLimit()!=null && results.size()>=config.getLimit()){ break; }
					}
					managedResultScanner.close();
					DRCounters.incClientNodeCustom(client.getType(), scanKeysVsRowsNumRows, getClientName(), getNodeName(),  
							DrCollectionTool.size(results));
					return results;
				}
			}).call();
	}
	
	//this method is in the node because it deals with the messy primaryKeyHasUnnecessaryTrailingSeparatorByte
	public byte[] getKeyBytesWithScatteringPrefix(List<Field<?>> overrideScatteringPrefixFields, PK key, boolean increment){
		//return only scatteringPrefix bytes
		if(key==null){
			if(DrCollectionTool.isEmpty(overrideScatteringPrefixFields)){
				return new byte[]{};
			}
			return FieldSetTool.getConcatenatedValueBytes(overrideScatteringPrefixFields, false, false);
		}
		
		//else return scatteringPrefix bytes + keyBytes + (maybe) trailing separator
		List<Field<?>> scatteringPrefixFields = DrListTool.createLinkedList();
		if(DrCollectionTool.notEmpty(overrideScatteringPrefixFields)){
			scatteringPrefixFields.addAll(overrideScatteringPrefixFields);
		}else{
			//maybe Assert the override fields match those returned for the key
			scatteringPrefixFields.addAll(fieldInfo.getSampleScatteringPrefix().getScatteringPrefixFields(key));
		}
		byte[] scatteringPrefixBytes = FieldSetTool.getConcatenatedValueBytes(scatteringPrefixFields, true, false);
		byte[] keyBytes = FieldSetTool.getConcatenatedValueBytes(key.getFields(), true, false);
		if(increment){
			keyBytes = DrByteTool.unsignedIncrement(keyBytes);
		}
		return DrByteTool.concatenate(scatteringPrefixBytes, keyBytes);
	}
	
	private <T extends Comparable<? super T>> void sortIfScatteringPrefixExists(List<T> ins){
		if(fieldInfo.getSampleScatteringPrefix().getNumPrefixBytes() > 0){
			Collections.sort(ins);
		}
	}
	
	public HBaseTaskNameParams getTaskNameParams(){
		return taskNameParams;
	}
	
	
}
