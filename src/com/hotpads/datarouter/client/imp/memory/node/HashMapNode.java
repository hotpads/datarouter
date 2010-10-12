package com.hotpads.datarouter.client.imp.memory.node;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;

public class HashMapNode<PK extends PrimaryKey<PK>,D extends Databean<PK>> 
extends HashMapReaderNode<PK,D>
implements MapStorage<PK,D>
{
	
	public HashMapNode(Class<D> databeanClass, 
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HashMapNode(Class<D> databeanClass, 
			DataRouter router, String clientName) {
		super(databeanClass, router, clientName);
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return this;
	}
	
	
	/************************************ MapStorageWriter methods ****************************/

	@Override
	public void delete(PK key, Config config) {
		if(key==null){ return; }
		this.backingMap.remove(key);
	}
	
	@Override
	public void deleteMulti(Collection<PK> keys, Config config) {
		for(Key<PK> key : CollectionTool.nullSafe(keys)){
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
