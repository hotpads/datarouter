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
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.util.DRCounters;

public abstract class HBaseEntityReaderNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>
extends BasePhysicalEntityNode<EK,E>{


	protected EntityNodeParams<EK,E> entityNodeParams;
	private HBaseTaskNameParams taskNameParams;//currently acting as a cache of superclass fields
	private HBaseEntityQueryBuilder<EK,E> queryBuilder;
	private HBaseEntityResultParser<EK,E> resultParser;
	
	public HBaseEntityReaderNode(DataRouter router, EntityNodeParams<EK,E> entityNodeParams,
			HBaseTaskNameParams taskNameParams){
		super(router.getContext(), entityNodeParams, taskNameParams);
		this.entityNodeParams = entityNodeParams;
		this.taskNameParams = taskNameParams;
		initNodes(router, taskNameParams.getClientName());
		//need to call initNodes before this so nodeByQualifierPrefix gets initialized
		this.queryBuilder = new HBaseEntityQueryBuilder<EK,E>(getEntityFieldInfo());
		this.resultParser = new HBaseEntityResultParser<EK,E>(entityFieldInfo,
				(Map<String,HBaseSubEntityReaderNode<EK,E,?,?,?>>)getNodeByQualifierPrefix());
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
	

	
	@Override
	public E getEntity(final EK ek, Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		return new HBaseMultiAttemptTask<E>(new HBaseTask<E>(getContext(), getTaskNameParams(), "getEntity", config){
				public E hbaseCall() throws Exception{
					byte[] rowBytes = queryBuilder.getRowBytesWithPartition(ek);
					Get get = new Get(rowBytes);
					Result hBaseResult = hTable.get(get);
					E entity = resultParser.parseEntity(ek, hBaseResult);
					if(entity != null){
						DRCounters.incSuffixClientNode(client.getType(), "entity databeans", getClientName(),
								getNodeName(), entity.getNumDatabeans());
					}
					return entity;
				}
			}).call();
	}
	
}
