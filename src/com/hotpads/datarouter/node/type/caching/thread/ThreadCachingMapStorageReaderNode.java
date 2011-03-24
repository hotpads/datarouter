package com.hotpads.datarouter.node.type.caching.thread;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.IndexedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.node.type.caching.thread.base.BaseThreadCachingNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public abstract class ThreadCachingMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedMapStorageNode<PK,D>>
extends BaseThreadCachingNode<PK,D,N>
implements MapStorageReaderNode<PK,D>{
	
	public ThreadCachingMapStorageReaderNode(N backingNode) {
		super(backingNode);
	}


	/************************* util ***************************/
	
	public static boolean useCache(final Config config){
		if(config==null){ return Config.DEFAULT_CACHE_OK; }
		return config.getCacheOk();
	}

	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(PK key, Config config){
		if(!useCache(config)){ return this.backingNode.exists(key, config); }
		if(getMapCacheForThisThread().containsKey(key)){ return true; }
		return backingNode.exists(key, config);
	}

	@Override
	public D get(PK key, Config config) {
		if(!useCache(config)){ return backingNode.get(key, config); }
		D cachedObject = getMapCacheForThisThread().get(key);
		if(cachedObject != null){ return cachedObject; }
		D realObject = backingNode.get(key, config);
		if(realObject != null){
			getMapCacheForThisThread().put(realObject.getKey(), realObject);
		}
		return realObject;
	}

	@Override
	public List<D> getAll(Config config) {
		//TODO keep track of whether the whole backing node is in the cache and return it if so
		return backingNode.getAll(config);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config) {
		if(!useCache(config)){ return backingNode.getMulti(keys, config); }
		List<D> resultBuilder = ListTool.createArrayList();
		Set<PK> uncachedKeys = SetTool.createHashSet();
		Map<PK,D> mapCacheForThisThread = getMapCacheForThisThread();
		for(PK key : CollectionTool.nullSafe(keys)){
			D databean = mapCacheForThisThread.get(key);
			if(databean != null){
				resultBuilder.add(databean);
			}else{
				uncachedKeys.add(key);
			}
		}
		List<D> fromBackingNode = this.backingNode.getMulti(uncachedKeys, config);
		ListTool.nullSafeArrayAddAll(resultBuilder, fromBackingNode);
		for(D databean : CollectionTool.nullSafe(resultBuilder)){
			mapCacheForThisThread.put(databean.getKey(), databean);
		}
		return resultBuilder;
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config) {
		if(!useCache(config)){ return backingNode.getKeys(keys, config); }
		List<PK> resultBuilder = ListTool.createArrayList();
		Set<PK> uncachedKeys = SetTool.createHashSet();
		Map<PK,D> mapCacheForThisThread = getMapCacheForThisThread();
		for(PK key : CollectionTool.nullSafe(keys)){
			D databean = mapCacheForThisThread.get(key);
			if(databean != null){
				resultBuilder.add(databean.getKey());
			}else{
				uncachedKeys.add(key);
			}
		}
		List<PK> fromBackingNode = backingNode.getKeys(uncachedKeys, config);
		ListTool.nullSafeArrayAddAll(resultBuilder, fromBackingNode);
		return resultBuilder;
	}
	
}
