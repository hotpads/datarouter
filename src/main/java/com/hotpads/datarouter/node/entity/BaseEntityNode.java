package com.hotpads.datarouter.node.entity;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTaskNameParams;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.MapTool;

public abstract class BaseEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>
implements EntityNode<EK,E>{

	private DataRouterContext drContext;
	private HBaseTaskNameParams taskNameParams;
	private Map<String,Node<?,?>> nodeByTableName;
	
	
	public BaseEntityNode(DataRouterContext drContext, HBaseTaskNameParams taskNameParams){
		this.drContext = drContext;
		this.taskNameParams = taskNameParams;
		this.nodeByTableName = MapTool.createHashMap();
	}

	protected <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>> void register(Node<PK,D> node){
		String tableName = Preconditions.checkNotNull(node.getFieldInfo().getTableName());
		nodeByTableName.put(tableName, node);
	}


	@Override
	public DataRouterContext getContext(){
		return drContext;
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
	public String getName(){
		return taskNameParams.getNodeName();
	}
	
}
