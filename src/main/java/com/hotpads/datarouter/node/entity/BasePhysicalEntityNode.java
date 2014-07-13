package com.hotpads.datarouter.node.entity;

import java.util.Map;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTaskNameParams;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;

public abstract class BasePhysicalEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>
extends BaseEntityNode<EK,E>
implements PhysicalEntityNode<EK,E>{

	private HBaseTaskNameParams taskNameParams;//currently acting as a cache of superclass fields
	private Map<String,Node<?,?>> nodeByQualifierPrefix;
	
	
	public BasePhysicalEntityNode(DataRouterContext drContext, HBaseTaskNameParams taskNameParams){
		super(drContext, taskNameParams.getNodeName());
		this.taskNameParams = taskNameParams;
	}


	protected <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>> void register(SubEntitySortedMapStorageReaderNode<EK,PK,D,?> node){
		super.register(node);
		nodeByQualifierPrefix.put(node.getEntityNodePrefix(), node);
	}

	
	@Override
	public Client getClient(){
		return getContext().getClientPool().getClient(getClientName());
	}

	@Override
	public String getClientName(){
		return taskNameParams.getClientName();
	}

	@Override
	public String getTableName(){
		return taskNameParams.getTableName();
	}
	
	@Override
	public Map<String,Node<?,?>> getNodeByQualifierPrefix(){
		return nodeByQualifierPrefix;
	}
	
	
}
