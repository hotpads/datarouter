package com.hotpads.datarouter.node.type.caching.map;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.node.type.caching.map.base.BaseMapCachingNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

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
		if(!useCache(config)){ return this.backingNode.exists(key, config); }
		try{
			updateLastAttemptedContact();
			if(cachingNode.exists(key, CACHE_CONFIG)){ countHits(1); return true; }
			updateLastContact();
		}catch(Exception e){
			countExceptions();
			return backingNode.exists(key, config);
		}
		countMisses(1);
		return backingNode.exists(key, config);
	}

	@Override
	public D get(PK key, Config config){
		if(!useCache(config)){ return backingNode.get(key, config); }
		D cachedObject = null;
		try{
			updateLastAttemptedContact();
			cachedObject = cachingNode.get(key, CACHE_CONFIG);
			updateLastContact();
		}catch(Exception e){
			countExceptions();
			return backingNode.get(key, config);
		}
		if(cachedObject != null){ countHits(1); return cachedObject; }
		D realObject = backingNode.get(key, config);
		if(realObject != null){
			countMisses(1);
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
	public List<D> getAll(Config config) {
		return backingNode.getAll(config);
	}

	@Override
	public List<D> getMulti(final Collection<PK> keys, Config config) {
		if(!useCache(config)){ return backingNode.getMulti(keys, config); }
		List<D> resultBuilder = ListTool.createLinkedList();
		try{
			updateLastAttemptedContact();
			resultBuilder.addAll(cachingNode.getMulti(keys, CACHE_CONFIG));
			updateLastContact();
		}catch(Exception e){
			countExceptions();
			return backingNode.getMulti(keys, config);
		}
		countHits(resultBuilder);
		Set<PK> cachedKeys = SetTool.createHashSet(KeyTool.getKeys(resultBuilder));
		Set<PK> uncachedKeys = SetTool.createHashSet();
		for(PK key : IterableTool.nullSafe(keys)){
			if(!cachedKeys.contains(key)){ uncachedKeys.add(key); }
		}
		if(CollectionTool.isEmpty(uncachedKeys)){ return resultBuilder; }
		List<D> fromBackingNode = backingNode.getMulti(uncachedKeys, config);
		countMisses(fromBackingNode);
		if(cacheReads){
			try{
				updateLastAttemptedContact();
				cachingNode.putMulti(fromBackingNode, CACHE_CONFIG);
				updateLastContact();
			}catch(Exception e){
				countExceptions();
			}
		}
		ListTool.nullSafeArrayAddAll(resultBuilder, fromBackingNode);
		return resultBuilder;
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config) {
		if(!useCache(config)){ return backingNode.getKeys(keys, config); }
		List<PK> resultBuilder = ListTool.createLinkedList();
		try{
			updateLastAttemptedContact();
			resultBuilder.addAll(cachingNode.getKeys(keys, CACHE_CONFIG));
			updateLastContact();
		}catch(Exception e){
			countExceptions();
			return backingNode.getKeys(keys, config);
		}
		countHits(resultBuilder);
		Set<PK> cachedKeys = SetTool.createHashSet(resultBuilder);
		Set<PK> uncachedKeys = SetTool.createHashSet();
		for(PK key : IterableTool.nullSafe(keys)){
			if(!cachedKeys.contains(key)){ uncachedKeys.add(key); }
		}
		if(CollectionTool.isEmpty(uncachedKeys)){ return resultBuilder; }
		List<D> fromBackingNode = backingNode.getMulti(uncachedKeys, config);
		countMisses(fromBackingNode);
		if(cacheReads){
			try{
				updateLastAttemptedContact();
				cachingNode.putMulti(fromBackingNode, CACHE_CONFIG);
				updateLastContact();
			}catch(Exception e){
				countExceptions();
			}
		}
		ListTool.nullSafeArrayAddAll(resultBuilder, KeyTool.getKeys(fromBackingNode));
		return resultBuilder;
	}
	
	/************** counters ***************************************/
	
	protected void countHits(int num){
		DRCounters.incSuffixOp(ClientType.memory, getName()+" hit");
//		logger.warn("hit");
	}
	
	protected void countMisses(int num){
		DRCounters.incSuffixOp(ClientType.memory, getName()+" miss");
//		logger.warn("miss");
	}
	
	protected void countExceptions(){
		DRCounters.incSuffixOp(ClientType.memory, getName()+" exception");
//		logger.warn("exception");
	}
	
	protected void countHits(Collection<?> c){
		countHits(CollectionTool.size(c));
	}
	
	protected void countMisses(Collection<?> c){
		countMisses(CollectionTool.size(c));
	}
}