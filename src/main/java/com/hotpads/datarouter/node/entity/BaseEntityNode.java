package com.hotpads.datarouter.node.entity;

import java.util.Map;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.Entity.Entity;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.MapTool;

public abstract class BaseEntityNode<EK extends EntityKey<EK>,E extends Entity<EK>>
implements EntityNode<EK,E>{

	private String name;
	private Map<String,Node<?,?>> nodeByName;
	
	
	public BaseEntityNode(String name){
		this.name = name;
		this.nodeByName = MapTool.createHashMap();
	}

	protected <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>> void register(Node<PK,D> node){
		nodeByName.put(node.getName(), node);
	}
	
	@Override
	public String getName(){
		return name;
	}
	
}
