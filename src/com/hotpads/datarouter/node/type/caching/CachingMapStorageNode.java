package com.hotpads.datarouter.node.type.caching;

import java.util.Collection;
import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;

public abstract class CachingMapStorageNode<D extends Databean,N extends MapStorageNode<D>>
extends CachingMapStorageReaderNode<D,N>
implements MapStorageNode<D>{

	
	public CachingMapStorageNode(N backingNode) {
		super(backingNode);
	}

	/***************************** MapStorageWriter ****************************/

	@Override
	public void delete(Key<D> key, Config config) {
		this.getMapCacheForThisThread().remove(key);
		this.backingNode.delete(key, config);
	}

	@Override
	public void deleteAll(Config config) {
		this.getMapCacheForThisThread().clear();
		this.backingNode.deleteAll(config);
	}

	@Override
	public void deleteMulti(Collection<? extends Key<D>> keys, Config config) {
		for(Key<D> key : CollectionTool.nullSafe(keys)){
			this.getMapCacheForThisThread().remove(key);
		}
		this.backingNode.deleteMulti(keys, config);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void put(D databean, Config config) {
		if(databean==null || databean.getKey()==null){ return; }
		this.backingNode.put(databean, config);
		if(CachingMapStorageReaderNode.useCache(config)){
			this.getMapCacheForThisThread().put(databean.getKey(), databean);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		this.backingNode.putMulti(databeans, config);
		Map<Key<D>,D> cacheForThisThread = this.getMapCacheForThisThread();
		for(D databean : CollectionTool.nullSafe(databeans)){
			cacheForThisThread.put(databean.getKey(), databean);
		}
	}
	
	
}
