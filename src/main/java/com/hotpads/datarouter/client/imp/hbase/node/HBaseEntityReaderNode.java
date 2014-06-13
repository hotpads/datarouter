package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.task.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseEntityResultTool;
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
//		return new HBaseMultiAttemptTask<D>(new HBaseTask<D>(getDataRouterContext(), "get", this, config){
//				public D hbaseCall() throws Exception{
//					byte[] rowBytes = getRowBytes(key.getEntityKey());
//					Get get = new Get(rowBytes);
//					byte[] qualifierPkBytes = getQualifierPkBytes(key);
//					byte[] qualifierPrefix = ByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(), 
//							qualifierPkBytes);
//					get.setFilter(new ColumnPrefixFilter(qualifierPrefix));
//					Result row = hTable.get(get);
//					if(row.isEmpty()){ return null; }
//					List<D> results = HBaseEntityResultTool.getDatabeansWithMatchingQualifierPrefix(row, fieldInfo);
//					if(CollectionTool.hasMultiple(results)){ 
//						throw new RuntimeException("shouldn't have multiple databeans"); 
//					}
//					return CollectionTool.getFirst(results);
//				}
//			}).call();
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
					Result[] hBaseResults = hTable.get(gets);
					List<D> results = ListTool.createArrayList();
					for(Result row : hBaseResults){
						if(row.isEmpty()){ continue; }
						List<D> databeansFromSingleGet = HBaseEntityResultTool.getDatabeansWithMatchingQualifierPrefix(
								row, fieldInfo);
						results.addAll(CollectionTool.nullSafe(databeansFromSingleGet));
					}
					return results;
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
						List<PK> pksFromSingleGet = HBaseEntityResultTool.getPrimaryKeysWithMatchingQualifierPrefix(
								row, fieldInfo);
						results.addAll(CollectionTool.nullSafe(pksFromSingleGet));
					}
					return results;
				}
			}).call();
	}

		
	
	/***************************** helper methods **********************************/
	
	private byte[] getRowBytes(EK entityKey){
		if(entityKey==null){ return new byte[]{}; }
		return FieldSetTool.getConcatenatedValueBytes(entityKey.getFields(), true, false);
	}
	
	private byte[] getQualifierPkBytes(PK primaryKey){
		if(primaryKey==null){ return new byte[]{}; }
		return FieldSetTool.getConcatenatedValueBytes(primaryKey.getPostEntityKeyFields(), true, true);
	}
	
}
