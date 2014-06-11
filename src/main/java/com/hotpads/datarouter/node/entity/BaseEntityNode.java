package com.hotpads.datarouter.node.entity;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.Entity.Entity;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.MapTool;

public abstract class BaseEntityNode<EK extends EntityKey<EK>,E extends Entity<EK>>
implements EntityNode<EK,E>{

	private String name;
	private Map<String,Node<?,?>> nodeByTableName;
	
	
	public BaseEntityNode(String name){
		this.name = name;
		this.nodeByTableName = MapTool.createHashMap();
	}

	protected <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>> void register(Node<PK,D> node){
		String tableName = Preconditions.checkNotNull(node.getFieldInfo().getTableName());
		nodeByTableName.put(tableName, node);
	}
	
	@Override
	public String getName(){
		return name;
	}
	
}
