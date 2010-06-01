package com.hotpads.datarouter.node.type.caching;

import java.util.Collection;
import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedSortedStorageNode;
import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;

public abstract class CachingMapStorageNode<D extends Databean<PK>,PK extends PrimaryKey<PK>,
N extends IndexedSortedStorageNode<D,PK>>
extends CachingMapStorageReaderNode<D,PK,N>
implements MapStorageNode<D,PK>{

	
	public CachingMapStorageNode(N backingNode) {
		super(backingNode);
	}

	/***************************** MapStorageWriter ****************************/

	@Override
	public void delete(UniqueKey<PK> key, Config config) {
		this.getMapCacheForThisThread().remove(key);
		this.backingNode.delete(key, config);
	}

	@Override
	public void deleteAll(Config config) {
		this.getMapCacheForThisThread().clear();
		this.backingNode.deleteAll(config);
	}

	@Override
	public void deleteMulti(Collection<? extends UniqueKey<PK>> keys, Config config) {
		for(Key<PK> key : CollectionTool.nullSafe(keys)){
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
		Map<UniqueKey<PK>,D> cacheForThisThread = this.getMapCacheForThisThread();
		for(D databean : CollectionTool.nullSafe(databeans)){
			cacheForThisThread.put(databean.getKey(), databean);
		}
	}
	
	
}
