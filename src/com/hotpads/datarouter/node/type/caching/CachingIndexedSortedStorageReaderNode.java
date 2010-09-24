package com.hotpads.datarouter.node.type.caching;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedSortedStorageNode;
import com.hotpads.datarouter.node.op.IndexedSortedStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.iterable.PeekableIterable;

public abstract class CachingIndexedSortedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK>,
N extends IndexedSortedStorageNode<PK,D>>
extends CachingMapStorageReaderNode<PK,D,N>
implements IndexedSortedStorageReaderNode<PK,D>{	
	
	public CachingIndexedSortedStorageReaderNode(N backingNode) {
		super(backingNode);
	}
	
	/************************* cache access *******************************/

	// lookupCache
	protected Map<String,Map<Lookup<PK>,List<D>>> lookupCacheByThreadName = MapTool.createHashMap();

	protected Map<Lookup<PK>,List<D>> getLookupCacheForThisThread(){
		String threadName = Thread.currentThread().getName();
		if(lookupCacheByThreadName.get(threadName)==null){
			lookupCacheByThreadName.put(threadName, new HashMap<Lookup<PK>,List<D>>());
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
	public PK getFirstKey(Config config) {
		//TODO implement caching
		return this.backingNode.getFirstKey(config);
	}

	@Override
	public List<D> getPrefixedRange(
			PK prefix, boolean wildcardLastField,
			PK start, boolean startInclusive, Config config) {
		//TODO implement caching
		return this.backingNode.getPrefixedRange(
				prefix, wildcardLastField, start, startInclusive, config);
	}

	@Override
	public List<PK> getKeysInRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config) {
		//TODO implement caching
		return this.backingNode.getKeysInRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config) {
		//TODO implement caching
		return this.backingNode.getRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		//TODO implement caching
		return this.backingNode.getWithPrefix(prefix, wildcardLastField, config);
	}

	@Override
	public List<D> getWithPrefixes(Collection<? extends PK> prefixes, boolean wildcardLastField, Config config) {
		//TODO implement caching
		return this.backingNode.getWithPrefixes(prefixes, wildcardLastField, config);
	}
	
	@Override
	public PeekableIterable<PK> scanKeys(PK startKey, boolean startInclusive, PK end, boolean endInclusive, Config config){
		return this.backingNode.scanKeys(startKey,startInclusive, end, endInclusive, config);
	};
	
	@Override
	public PeekableIterable<D> scan(PK startKey, boolean startInclusive, PK end, boolean endInclusive, Config config){
		return this.backingNode.scan(startKey,startInclusive, end, endInclusive, config);
	};

	/***************** IndexedStorageReader ************************************/

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		D realObject = this.backingNode.lookupUnique(uniqueKey, config);
		if( ! CachingMapStorageReaderNode.useCache(config)){
			return realObject;
		}
		if(realObject != null){
			//TODO add a secondary key cache
			this.getMapCacheForThisThread().put(realObject.getKey(), realObject);
		}
		return realObject;
	};

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		List<D> realObjects = this.backingNode.lookupMultiUnique(uniqueKeys, config);
		if( ! CachingMapStorageReaderNode.useCache(config)){
			return realObjects;
		}
		Map<PK,D> mapCacheForThisThread = this.getMapCacheForThisThread();
		//TODO add a secondary key cache
		for(D databean : CollectionTool.nullSafe(realObjects)){
			mapCacheForThisThread.put(databean.getKey(), databean);
		}
		return realObjects;
	};
	
	@Override
	public List<D> lookup(Lookup<PK> lookup, Config config) {
		if( ! CachingMapStorageReaderNode.useCache(config)){
			return this.backingNode.lookup(lookup, config);
		}
		List<D> fromLookupCache = this.getLookupCacheForThisThread().get(lookup);
		if(fromLookupCache != null){ return fromLookupCache; }
		List<D> fromBackingNode = this.backingNode.lookup(lookup, config);
		Map<PK,D> mapCacheForThisThread = this.getMapCacheForThisThread();
		for(D databean : CollectionTool.nullSafe(fromBackingNode)){
			mapCacheForThisThread.put(databean.getKey(), databean);
		}
		this.getLookupCacheForThisThread().put(lookup, fromBackingNode);
		return fromBackingNode;
	}
	
	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config config) {
		if( ! CachingMapStorageReaderNode.useCache(config)){
			return this.backingNode.lookup(lookups, config);
		}
		Set<Lookup<PK>> lookupMisses = SetTool.createHashSet();
		Set<D> resultBuilder = SetTool.createHashSet();
		for(Lookup<PK> lookup : CollectionTool.nullSafe(lookups)){
			List<D> fromLookupCache = this.getLookupCacheForThisThread().get(lookup);
			if(fromLookupCache==null){ lookupMisses.add(lookup); }
			else{ SetTool.nullSafeHashAddAll(resultBuilder, fromLookupCache); }
		}
		List<D> fromBackingNode = this.backingNode.lookup(lookupMisses, config);
		SetTool.nullSafeHashAddAll(resultBuilder, fromBackingNode);
		List<D> result = ListTool.createArrayList(resultBuilder);
		Map<PK,D> mapCacheForThisThread = this.getMapCacheForThisThread();
		for(D databean : CollectionTool.nullSafe(result)){
			mapCacheForThisThread.put(databean.getKey(), databean);
		}
		//can't cache the individual lookups because the result is lumped together
		
		//TODO maybe return a List<List<D>> so they can be cached
		return result;
	}
	
}
