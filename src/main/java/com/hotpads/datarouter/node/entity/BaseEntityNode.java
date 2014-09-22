package com.hotpads.datarouter.node.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;

public abstract class BaseEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>
implements EntityNode<EK,E>{

	private DataRouterContext drContext;
	private String name;
	private List<Node<?,?>> subEntityNodes;
	
	
	public BaseEntityNode(DataRouterContext drContext, String name){
		this.drContext = drContext;
		this.name = name;
		this.subEntityNodes = new ArrayList<>();
	}

	//registering doesn't currently do much, but gives us access to a list of subEntityNodes
	protected <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>> void register(SortedMapStorageReaderNode<PK,D> subEntityNode){
		subEntityNodes.add(subEntityNode);
	}


	@Override
	public DataRouterContext getContext(){
		return drContext;
	}
	
	@Override
	public String getName(){
		return name;
	}

	@Override
	public Collection<Node<?,?>> getSubEntityNodes(){
		return subEntityNodes;
	}
	
}
