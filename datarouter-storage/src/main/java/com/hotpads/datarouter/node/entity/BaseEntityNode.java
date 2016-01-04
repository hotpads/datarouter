package com.hotpads.datarouter.node.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;

public abstract class BaseEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>
implements EntityNode<EK,E>{

	private Datarouter datarouter;
	private String name;
	private List<Node<?,?>> subEntityNodes;
	
	
	public BaseEntityNode(Datarouter datarouter, String name){
		this.datarouter = datarouter;
		this.name = name;
		this.subEntityNodes = new ArrayList<>();
	}

	@Override
	public <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>> void register(
			SortedMapStorageReaderNode<PK,D> subEntityNode){
		subEntityNodes.add(subEntityNode);
	}


	@Override
	public Datarouter getContext(){
		return datarouter;
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
