package com.hotpads.datarouter.node.entity;

import java.util.HashMap;
import java.util.Map;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTaskNameParams;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fieldcache.EntityFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;

public abstract class BasePhysicalEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>
extends BaseEntityNode<EK,E>
implements PhysicalEntityNode<EK,E>{

//	private EntityNodeParams<EK,E> entityNodeParams;
	protected EntityFieldInfo<EK,E> entityFieldInfo;
	private HBaseTaskNameParams taskNameParams;//currently acting as a cache of superclass fields
	private Map<String,SubEntitySortedMapStorageReaderNode<EK,?,?,?>> nodeByQualifierPrefix;
	
	
	public BasePhysicalEntityNode(DataRouterContext drContext, EntityNodeParams<EK,E> entityNodeParams,
			HBaseTaskNameParams taskNameParams){
		super(drContext, taskNameParams.getNodeName());
//		this.entityNodeParams = entityNodeParams;
		this.entityFieldInfo = new EntityFieldInfo<>(entityNodeParams);
		this.taskNameParams = taskNameParams;
		this.nodeByQualifierPrefix = new HashMap<>();
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
	public Map<String,? extends SubEntitySortedMapStorageReaderNode<EK,?,?,?>> getNodeByQualifierPrefix(){
		return nodeByQualifierPrefix;
	}
	
//	public EntityNodeParams<EK,E> getEntityNodeParams(){
//		return entityNodeParams;
//	}
	public EntityFieldInfo<EK,E> getEntityFieldInfo(){
		return entityFieldInfo;
	}
	
}
