package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.task.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseQueryBuilder;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.collections.Range;

public class HBaseEntityReaderNode<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BasePhysicalNode<PK,D,F>
implements HBasePhysicalNode<PK,D>,
		MapStorageReader<PK,D>
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
		return new HBaseMultiAttemptTask<D>(new HBaseTask<D>(getDataRouterContext(), "get", this, config){
				public D hbaseCall() throws Exception{
					byte[] rowBytes = getRowBytes(key.getEntityKey());
					Get get = new Get(rowBytes);
					get.setFilter(new ColumnPrefixFilter(fieldInfo.getEntityColumnPrefixBytes()));
					Result row = hTable.get(get);
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
		return new HBaseMultiAttemptTask<List<D>>(new HBaseTask<List<D>>(getDataRouterContext(), "getMulti", this, config){
				public List<D> hbaseCall() throws Exception{
					DRCounters.incSuffixClientNode(client.getType(), "getMulti rows", getClientName(), node.getName(), 
							CollectionTool.size(keys));
					List<Get> gets = ListTool.createArrayListWithSize(keys);
					for(PK key : keys){
						byte[] rowBytes = getRowBytesWithScatteringPrefix(null, key, false);
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
		return new HBaseMultiAttemptTask<List<PK>>(new HBaseTask<List<PK>>(getDataRouterContext(), "getKeys", this, config){
				public List<PK> hbaseCall() throws Exception{
					DRCounters.incSuffixClientNode(client.getType(), "getKeys rows", getClientName(), node.getName(), 
							CollectionTool.size(keys));
					List<Get> gets = ListTool.createArrayListWithSize(keys);
					for(PK key : keys){
						byte[] rowBytes = getRowBytesWithScatteringPrefix(null, key, false);
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
		return new HBaseMultiAttemptTask<List<Result>>(new HBaseTask<List<Result>>(getDataRouterContext(), scanKeysVsRowsNumBatches,
				this, config){
				public List<Result> hbaseCall() throws Exception{
					byte[] start = range.getStart().copyToNewArray();
					if(start!=null && !range.getStartInclusive()){//careful: this may have already been set by scatteringPrefix logic
						start = ByteTool.unsignedIncrement(start);
					}
					byte[] end = range.getEnd() == null? null : range.getEnd().copyToNewArray();
//					if(end!=null && range.getEndInclusive()){//careful: this may have already been set by scatteringPrefix logic
//						end = ByteTool.unsignedIncrement(end);
//					}
					
					//start/endInclusive already adjusted for
					Scan scan = HBaseQueryBuilder.getScanForRange(start, true, end, range.getEndInclusive(), config);
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
	
	private byte[] getRowBytes(EK entityKey){
		if(entityKey==null){ return new byte[]{}; }
		return FieldSetTool.getConcatenatedValueBytes(entityKey.getFields(), true, false);
	}
	
}
