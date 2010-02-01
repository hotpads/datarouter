package com.hotpads.datarouter.client.imp.hashmap.node;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalMapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;

public class HashMapNode<D extends Databean> 
extends HashMapReaderNode<D>
implements PhysicalMapStorageNode<D>
{
	
	public HashMapNode(Class<D> databeanClass, DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HashMapNode(Class<D> databeanClass, DataRouter router, String clientName) {
		super(databeanClass, router, clientName);
	}
	
	@Override
	public Node<D> getMaster() {
		return this;
	}
	
	
	/************************************ MapStorageWriter methods ****************************/

	@Override
	public void delete(Key<D> key, Config config) {
		if(key==null){ return; }
		this.backingMap.remove(key);
	}
	
	@Override
	public void deleteMulti(Collection<? extends Key<D>> keys, Config config) {
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
