package com.hotpads.datarouter.node.entity;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
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
	private Map<String,Node<?,?>> nodeByTableName;
	
	
	public BaseEntityNode(DataRouterContext drContext, String name){
		this.drContext = drContext;
		this.name = name;
		this.nodeByTableName = new HashMap<>();
	}

	protected <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>> void register(SortedMapStorageReaderNode<PK,D> node){
		String tableName = Preconditions.checkNotNull(node.getFieldInfo().getTableName());
		nodeByTableName.put(tableName, node);
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
	public Map<String,Node<?,?>> getNodeByTableName(){
		return nodeByTableName;
	}
	
}
