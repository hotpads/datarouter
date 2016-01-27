package com.hotpads.datarouter.node.type.caching.map;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.node.type.caching.map.base.BaseMapCachingNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;

public class MapCachingMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageNode<PK,D>>
extends BaseMapCachingNode<PK,D,F,N>
implements MapStorageReaderNode<PK,D>{

	public static final Long CACHE_TIMEOUT_MS = 100L;

	public static final Config CACHE_CONFIG = new Config().setCacheTimeoutMs(CACHE_TIMEOUT_MS);

	protected boolean cacheReads = true;

	public MapCachingMapStorageReaderNode(N cacheNode, N backingNode, boolean cacheReads){
		super(cacheNode, backingNode);
		this.cacheReads = cacheReads;
	}

	/**************************** MapStorageReader ***********************************/

	@Override
	public boolean exists(PK key, Config config){
		if(!useCache(config)){
			return this.backingNode.exists(key, config);
		}
		try{
			updateLastAttemptedContact();
			if(cachingNode.exists(key, CACHE_CONFIG)){
				countHits();
				return true;
			}
			updateLastContact();
		}catch(Exception e){
			countExceptions();
			return backingNode.exists(key, config);
		}
		countMisses();
		return backingNode.exists(key, config);
	}

	@Override
	public D get(PK key, Config config){
		if(!useCache(config)){
			return backingNode.get(key, config);
		}
		D cachedObject = null;
		try{
			updateLastAttemptedContact();
			cachedObject = cachingNode.get(key, CACHE_CONFIG);
			updateLastContact();
		}catch(Exception e){
			countExceptions();
			return backingNode.get(key, config);
		}
		if(cachedObject != null){
			countHits();
			return cachedObject;
		}
		D realObject = backingNode.get(key, config);
		if(realObject != null){
			countMisses();
			if(cacheReads){
				try{
					updateLastAttemptedContact();
					cachingNode.put(realObject, CACHE_CONFIG);
					updateLastContact();
				}catch(Exception e){
					countExceptions();
				}
			}
		}
		return realObject;
	}

	@Override
	public List<D> getMulti(final Collection<PK> keys, Config config) {
		if(!useCache(config)){
			return backingNode.getMulti(keys, config);
		}
		List<D> resultBuilder = new LinkedList<>();
		try{
			updateLastAttemptedContact();
			resultBuilder.addAll(cachingNode.getMulti(keys, CACHE_CONFIG));
			updateLastContact();
		}catch(Exception e){
			countExceptions();
			return backingNode.getMulti(keys, config);
		}
		countHits();
		Set<PK> cachedKeys = new HashSet<>(DatabeanTool.getKeys(resultBuilder));
		Set<PK> uncachedKeys = new HashSet<>();
		for(PK key : DrIterableTool.nullSafe(keys)){
			if(!cachedKeys.contains(key)){
				uncachedKeys.add(key);
			}
		}
		if(DrCollectionTool.isEmpty(uncachedKeys)){
			return resultBuilder;
		}
		List<D> fromBackingNode = getAndCacheDatabeans(uncachedKeys, config);
		DrListTool.nullSafeArrayAddAll(resultBuilder, fromBackingNode);
		return resultBuilder;
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config) {
		if(!useCache(config)){
			return backingNode.getKeys(keys, config);
		}
		List<PK> resultBuilder = new LinkedList<>();
		try{
			updateLastAttemptedContact();
			resultBuilder.addAll(cachingNode.getKeys(keys, CACHE_CONFIG));
			updateLastContact();
		}catch(Exception e){
			countExceptions();
			return backingNode.getKeys(keys, config);
		}
		countHits();
		Set<PK> cachedKeys = new HashSet<>(resultBuilder);
		Set<PK> uncachedKeys = new HashSet<>();
		for(PK key : DrIterableTool.nullSafe(keys)){
			if(!cachedKeys.contains(key)){
				uncachedKeys.add(key);
			}
		}
		if(DrCollectionTool.isEmpty(uncachedKeys)){
			return resultBuilder;
		}
		List<D> fromBackingNode = getAndCacheDatabeans(uncachedKeys, config);
		DrListTool.nullSafeArrayAddAll(resultBuilder, DatabeanTool.getKeys(fromBackingNode));
		return resultBuilder;
	}

	private List<D> getAndCacheDatabeans(Collection<PK> uncachedKeys, Config config){
		List<D> fromBackingNode = backingNode.getMulti(uncachedKeys, config);
		countMisses();
		if(cacheReads){
			try{
				updateLastAttemptedContact();
				cachingNode.putMulti(fromBackingNode, CACHE_CONFIG);
				updateLastContact();
			}catch(Exception e){
				countExceptions();
			}
		}
		return fromBackingNode;
	}

	/************** counters ***************************************/

	private void countHits(){
		DRCounters.incOp(null, getName()+" hit");
	}

	private void countMisses(){
		DRCounters.incOp(null, getName()+" miss");
	}

	private void countExceptions(){
		DRCounters.incOp(null, getName()+" exception");
	}

}
