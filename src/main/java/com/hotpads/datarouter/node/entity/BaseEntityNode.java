package com.hotpads.datarouter.node.entity;

import java.util.Map;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.MapTool;

public class BaseEntityNode<EK extends EntityKey<EK>>
implements EntityNode<EK>{

	private String name;
	private Map nodeByPkClass;//generic types protected by accessor methods
	
	
	public BaseEntityNode(String name){
		this.name = name;
		this.nodeByPkClass = MapTool.createHashMap();
	}


	@Override
	public String getName(){
		return name;
	}
	
	private <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>> Map<PK,Node<PK,D>> register(Class<PK> pkClass,
			Node<PK,D> node){
		nodeByPkClass.put(pkClass, node);
		return nodeByPkClass;
	}
}
