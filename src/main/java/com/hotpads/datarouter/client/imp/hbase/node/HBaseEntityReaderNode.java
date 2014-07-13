package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.Map;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;

import com.hotpads.datarouter.client.imp.hbase.task.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTaskNameParams;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseEntityQueryBuilder;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseEntityResultParser;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.entity.BasePhysicalEntityNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;

public abstract class HBaseEntityReaderNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>
extends BasePhysicalEntityNode<EK,E>{

	private HBaseTaskNameParams taskNameParams;//currently acting as a cache of superclass fields
	private HBaseEntityQueryBuilder<EK,E> queryBuilder;
	private HBaseEntityResultParser<EK,E> resultParser;
	
	public HBaseEntityReaderNode(DataRouter router, HBaseTaskNameParams taskNameParams){
		super(router.getContext(), taskNameParams);
		this.taskNameParams = taskNameParams;
		initNodes(router, taskNameParams.getClientName());
		//need to call initNodes before this so nodeByQualifierPrefix gets initialized
		this.resultParser = new HBaseEntityResultParser<EK,E>((Map<String,HBaseSubEntityReaderNode<EK,?,?,?>>)getNodeByQualifierPrefix());
	}
	

	protected abstract void initNodes(DataRouter router, String clientName);

	@Override
	public HBaseClient getClient(){
		return (HBaseClient)super.getClient();
	}
	
	public HBaseTaskNameParams getTaskNameParams(){
		return taskNameParams;
	}
	
	public HBaseEntityResultParser<EK,E> getResultParser(){
		return resultParser;
	}
	
//	public Map<String,SubEntitySortedMapStorageReaderNode<EK,?,?,?>> getNodeByQualifierPrefix(){
//		return nodeByQualifierPrefix;
//	}
	
	protected abstract E parseHBaseResult(EK ek, Result result);

	
	@Override
	public E getEntity(final EK ek, Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		return new HBaseMultiAttemptTask<E>(new HBaseTask<E>(getContext(), getTaskNameParams(), "getEntity", config){
				public E hbaseCall() throws Exception{
					byte[] rowBytes = queryBuilder.getRowBytes(ek);
					Get get = new Get(rowBytes);
					Result hBaseResult = hTable.get(get);
					E entity = parseHBaseResult(ek, hBaseResult);
					return entity;
				}
			}).call();
	}
	
}
