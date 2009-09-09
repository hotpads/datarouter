package com.hotpads.datarouter.node.type.caching;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.caching.BaseCachingNode;
import com.hotpads.datarouter.node.op.MapStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;

public abstract class CachingMapStorageReaderNode<D extends Databean,N extends MapStorageReaderNode<D>>
extends BaseCachingNode<D,N>
implements MapStorageReaderNode<D>{
	
	
	public CachingMapStorageReaderNode(N backingNode) {
		super(backingNode);
	}

	
	/************************* cache access *******************************/

	protected Map<String,Map<Key<D>,D>> mapCacheByThreadName = MapTool.createHashMap();

	protected Map<Key<D>,D> getMapCacheForThisThread(){
		String threadName = Thread.currentThread().getName();
		if(mapCacheByThreadName.get(threadName)==null){
			mapCacheByThreadName.put(threadName, new HashMap<Key<D>,D>());
		}
		return mapCacheByThreadName.get(threadName);
	}
	
	protected void clearMapCacheForThisThread(){
		String threadName = Thread.currentThread().getName();
		this.mapCacheByThreadName.remove(threadName);
	}
	
	@Override
	public void clearThreadSpecificState(){
		this.clearMapCacheForThisThread();
	}

	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(Key<D> key, Config config){
		if(!useCache(config)){ return this.backingNode.exists(key, config); }
		if(this.getMapCacheForThisThread().containsKey(key)){ return true; }
		return this.backingNode.exists(key, config);
	}

	@SuppressWarnings("unchecked")
	@Override
	public D get(Key<D> key, Config config) {
		if(!useCache(config)){ return this.backingNode.get(key, config); }
		D cachedObject = this.getMapCacheForThisThread().get(key);
		if(cachedObject != null){ return cachedObject; }
		D realObject = this.backingNode.get(key, config);
		if(realObject != null){
			this.getMapCacheForThisThread().put(realObject.getKey(), realObject);
		}
		return realObject;
	}

	@Override
	public List<D> getAll(Config config) {
		//TODO keep track of whether the whole backing node is in the cache and return it if so
		return this.backingNode.getAll(config);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<D> getMulti(Collection<? extends Key<D>> keys, Config config) {
		if(!useCache(config)){ return this.backingNode.getMulti(keys, config); }
		List<D> resultBuilder = ListTool.createArrayList();
		Set<Key<D>> uncachedKeys = SetTool.createHashSet();
		Map<Key<D>,D> mapCacheForThisThread = this.getMapCacheForThisThread();
		for(Key<D> key : CollectionTool.nullSafe(keys)){
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

	/************************* util ***************************/
	
	public static boolean useCache(final Config config){
		if(config==null){ return Config.defaultCacheOk; }
		return config.getCacheOk();
	}
	
}
