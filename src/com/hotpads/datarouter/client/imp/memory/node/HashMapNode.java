package com.hotpads.datarouter.client.imp.memory.node;

import java.util.Collection;

import com.hotpads.datarouter.client.imp.memory.MemoryClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;

public class HashMapNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
extends HashMapReaderNode<PK,D,F>
implements MapStorage<PK,D>{
	
	public HashMapNode(Class<D> databeanClass, Class<F> fielderClass, 
			DataRouter router, MemoryClient client){
		super(databeanClass, fielderClass, router, client);
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return this;
	}
	
	
	/************************************ MapStorageWriter methods ****************************/

	@Override
	public void delete(PK key, Config config) {
		if(key==null){ return; }
		backingMap.remove(key);
	}
	
	@Override
	public void deleteMulti(Collection<PK> keys, Config config) {
		for(Key<PK> key : CollectionTool.nullSafe(keys)){
			backingMap.remove(key);
		}
	}
	
	
	@Override
	public void deleteAll(Config config) {
		backingMap.clear();
	}

	
	@Override
	public void put(final D databean, Config config) {
		if(databean==null || databean.getKey()==null){ return; }
		backingMap.put(databean.getKey(), databean);
	}

	
	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		for(D databean : CollectionTool.nullSafe(databeans)){
			put(databean, config);
		}
	}
	
	

	
}
