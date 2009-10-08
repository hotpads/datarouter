package com.hotpads.datarouter.node.type.caching;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedSortedStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.lookup.Lookup;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;

public abstract class CachingIndexedSortedStorageReaderNode<D extends Databean,N extends IndexedSortedStorageReaderNode<D>>
extends CachingMapStorageReaderNode<D,N>
implements IndexedSortedStorageReaderNode<D>{	
	
	public CachingIndexedSortedStorageReaderNode(N backingNode) {
		super(backingNode);
	}
	
	/************************* cache access *******************************/

	// lookupCache
	protected Map<String,Map<Lookup<D>,List<D>>> lookupCacheByThreadName = MapTool.createHashMap();

	protected Map<Lookup<D>,List<D>> getLookupCacheForThisThread(){
		String threadName = Thread.currentThread().getName();
		if(lookupCacheByThreadName.get(threadName)==null){
			lookupCacheByThreadName.put(threadName, new HashMap<Lookup<D>,List<D>>());
		}
		return lookupCacheByThreadName.get(threadName);
	}
	
	protected void clearLookupCacheForThisThread(){
		String threadName = Thread.currentThread().getName();
		this.lookupCacheByThreadName.remove(threadName);
	}
	
	// firstRecordCache
	protected Map<String,D> firstRecordByThreadName = MapTool.createHashMap();

	protected D getFirstRecordCacheForThisThread(){
		String threadName = Thread.currentThread().getName();
		return this.firstRecordByThreadName.get(threadName);
	}

	protected void setFirstRecordCacheForThisThread(D firstRecord){
		String threadName = Thread.currentThread().getName();
		this.firstRecordByThreadName.put(threadName, firstRecord);
	}
	
	protected void clearFirstRecordCacheForThisThread(){
		String threadName = Thread.currentThread().getName();
		this.firstRecordByThreadName.remove(threadName);
	}
	
	@Override
	public void clearThreadSpecificState(){
		super.clearThreadSpecificState();
		this.clearNonMapCaches();
	}
	
	public void clearNonMapCaches(){
		this.clearLookupCacheForThisThread();
		this.clearFirstRecordCacheForThisThread();
	}
	


	/***************** SortedStorageReader ************************************/

	@Override
	public D getFirst(Config config) {
		if( ! CachingMapStorageReaderNode.useCache(config)){
			return this.backingNode.getFirst(config);
		}
		D cachedFirstRecord = this.getFirstRecordCacheForThisThread();
		if(cachedFirstRecord != null){ return cachedFirstRecord; }
		D authoritativeFirstRecord = this.backingNode.getFirst(config);
		this.setFirstRecordCacheForThisThread(authoritativeFirstRecord);
		return authoritativeFirstRecord;
	}

	@Override
	public List<D> getRange(Key<D> start, boolean startInclusive, Key<D> end, boolean endInclusive, Config config) {
		//TODO implement caching
		return this.backingNode.getRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getWithPrefix(Key<D> prefix, boolean wildcardLastField, Config config) {
		//TODO implement caching
		return this.backingNode.getWithPrefix(prefix, wildcardLastField, config);
	}

	@Override
	public List<D> getWithPrefixes(Collection<? extends Key<D>> prefixes, boolean wildcardLastField, Config config) {
		//TODO implement caching
		return this.backingNode.getWithPrefixes(prefixes, wildcardLastField, config);
	}

	/***************** IndexedStorageReader ************************************/

	@SuppressWarnings("unchecked")
	@Override
	public List<D> lookup(Lookup<D> lookup, Config config) {
		if( ! CachingMapStorageReaderNode.useCache(config)){
			return this.backingNode.lookup(lookup, config);
		}
		List<D> fromLookupCache = this.getLookupCacheForThisThread().get(lookup);
		if(fromLookupCache != null){ return fromLookupCache; }
		List<D> fromBackingNode = this.backingNode.lookup(lookup, config);
		Map<Key<D>,D> mapCacheForThisThread = this.getMapCacheForThisThread();
		for(D databean : CollectionTool.nullSafe(fromBackingNode)){
			mapCacheForThisThread.put(databean.getKey(), databean);
		}
		this.getLookupCacheForThisThread().put(lookup, fromBackingNode);
		return fromBackingNode;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<D> lookup(Collection<? extends Lookup<D>> lookups, Config config) {
		if( ! CachingMapStorageReaderNode.useCache(config)){
			return this.backingNode.lookup(lookups, config);
		}
		Set<Lookup<D>> lookupMisses = SetTool.createHashSet();
		Set<D> resultBuilder = SetTool.createHashSet();
		for(Lookup<D> lookup : CollectionTool.nullSafe(lookups)){
			List<D> fromLookupCache = this.getLookupCacheForThisThread().get(lookup);
			if(fromLookupCache==null){ lookupMisses.add(lookup); }
			else{ SetTool.nullSafeHashAddAll(resultBuilder, fromLookupCache); }
		}
		List<D> fromBackingNode = this.backingNode.lookup(lookupMisses, config);
		SetTool.nullSafeHashAddAll(resultBuilder, fromBackingNode);
		List<D> result = ListTool.createArrayList(resultBuilder);
		Map<Key<D>,D> mapCacheForThisThread = this.getMapCacheForThisThread();
		for(D databean : CollectionTool.nullSafe(result)){
			mapCacheForThisThread.put(databean.getKey(), databean);
		}
		//can't cache the individual lookups because the result is lumped together
		
		//TODO maybe return a List<List<D>> so they can be cached
		return result;
	}
	
}
