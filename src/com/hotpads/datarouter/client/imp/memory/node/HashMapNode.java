package com.hotpads.datarouter.client.imp.memory.node;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalMapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;

public class HashMapNode<D extends Databean,PK extends PrimaryKey<D>> 
extends HashMapReaderNode<D,PK>
implements PhysicalMapStorageNode<D,PK>
{
	
	public HashMapNode(Class<PK> primaryKeyClass, 
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(primaryKeyClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HashMapNode(Class<PK> primaryKeyClass, 
			DataRouter router, String clientName) {
		super(primaryKeyClass, router, clientName);
	}
	
	@Override
	public Node<D,PK> getMaster() {
		return this;
	}
	
	
	/************************************ MapStorageWriter methods ****************************/

	@Override
	public void delete(UniqueKey<D> key, Config config) {
		if(key==null){ return; }
		this.backingMap.remove(key);
	}
	
	@Override
	public void deleteMulti(Collection<? extends UniqueKey<D>> keys, Config config) {
		for(Key<D> key : CollectionTool.nullSafe(keys)){
			this.backingMap.remove(key);
		}
	}
	
	
	@Override
	public void deleteAll(Config config) {
		this.backingMap.clear();
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public void put(final D databean, Config config) {
		if(databean==null || databean.getKey()==null){ return; }
		this.backingMap.put(databean.getKey(), databean);
	}

	
	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		for(D databean : CollectionTool.nullSafe(databeans)){
			this.put(databean, config);
		}
	}
	
	

	
}
