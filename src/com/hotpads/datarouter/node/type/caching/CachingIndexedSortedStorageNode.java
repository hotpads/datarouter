package com.hotpads.datarouter.node.type.caching;

import java.util.Collection;
import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedSortedStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.index.Lookup;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;

public class CachingIndexedSortedStorageNode<D extends Databean,N extends IndexedSortedStorageNode<D>>
extends CachingIndexedSortedStorageReaderNode<D,N>
implements IndexedSortedStorageNode<D>{

	
	public CachingIndexedSortedStorageNode(N backingNode) {
		super(backingNode);
	}

	/********************** indexed storage write ops ************************/

	/*
	 * MULTIPLE INHERITANCE
	 */
	
	@Override
	public void delete(Lookup<D> lookup, Config config) {
		this.clearNonMapCaches();
		this.backingNode.delete(lookup, config);
	}

	/***************************** MapStorageWriter ****************************/

	/*
	 * MULTIPLE INHERITANCE
	 */

	@Override
	public void delete(Key<D> key, Config config) {
		this.clearNonMapCaches();
		this.getMapCacheForThisThread().remove(key);
		this.backingNode.delete(key, config);
	}

	@Override
	public void deleteAll(Config config) {
		this.clearNonMapCaches();
		this.getMapCacheForThisThread().clear();
		this.backingNode.deleteAll(config);
	}

	@Override
	public void deleteMulti(Collection<? extends Key<D>> keys, Config config) {
		this.clearNonMapCaches();
		Map<Key<D>,D> mapCacheForThisThread = this.getMapCacheForThisThread();
		for(Key<D> key : CollectionTool.nullSafe(keys)){
			mapCacheForThisThread.remove(key);
		}
		this.backingNode.deleteMulti(keys, config);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void put(D databean, Config config) {
		this.clearNonMapCaches();
		if(databean==null || databean.getKey()==null){ return; }
		this.backingNode.put(databean, config);
		if(CachingMapStorageReaderNode.useCache(config)){
			this.getMapCacheForThisThread().put(databean.getKey(), databean);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		this.clearNonMapCaches();
		this.backingNode.putMulti(databeans, config);
		Map<Key<D>,D> mapCacheForThisThread = this.getMapCacheForThisThread();
		for(D databean : CollectionTool.nullSafe(databeans)){
			mapCacheForThisThread.put(databean.getKey(), databean);
		}
	}
	
	
}
