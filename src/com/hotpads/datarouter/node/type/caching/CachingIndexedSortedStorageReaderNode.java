package com.hotpads.datarouter.node.type.caching;

import com.hotpads.datarouter.node.op.combo.IndexedMapStorage;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class CachingIndexedSortedStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedMapStorage<PK,D>>
//extends CachingMapStorageReaderNode<PK,D,N>
//implements IndexedSortedMapStorageReader<PK,D>
{	
//	
//	public CachingIndexedSortedStorageReaderNode(N backingNode) {
//		super(backingNode);
//	}	
//
//
//	/***************** SortedStorageReader ************************************/
//
//	@Override
//	public D getFirst(Config config) {
//		if( ! CachingMapStorageReaderNode.useCache(config)){
//			return this.backingNode.getFirst(config);
//		}
//		D cachedFirstRecord = this.getFirstRecordCacheForThisThread();
//		if(cachedFirstRecord != null){ return cachedFirstRecord; }
//		D authoritativeFirstRecord = this.backingNode.getFirst(config);
//		this.setFirstRecordCacheForThisThread(authoritativeFirstRecord);
//		return authoritativeFirstRecord;
//	}
//
//	@Override
//	public PK getFirstKey(Config config) {
//		//TODO implement caching
//		return this.backingNode.getFirstKey(config);
//	}
//
//	@Override
//	public List<D> getPrefixedRange(
//			PK prefix, boolean wildcardLastField,
//			PK start, boolean startInclusive, Config config) {
//		//TODO implement caching
//		return this.backingNode.getPrefixedRange(
//				prefix, wildcardLastField, start, startInclusive, config);
//	}
//
//	@Override
//	public List<PK> getKeysInRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config) {
//		//TODO implement caching
//		return this.backingNode.getKeysInRange(start, startInclusive, end, endInclusive, config);
//	}
//
//	@Override
//	public List<D> getRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config) {
//		//TODO implement caching
//		return this.backingNode.getRange(start, startInclusive, end, endInclusive, config);
//	}
//
//	@Override
//	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
//		//TODO implement caching
//		return this.backingNode.getWithPrefix(prefix, wildcardLastField, config);
//	}
//
//	@Override
//	public List<D> getWithPrefixes(Collection<? extends PK> prefixes, boolean wildcardLastField, Config config) {
//		//TODO implement caching
//		return this.backingNode.getWithPrefixes(prefixes, wildcardLastField, config);
//	}
//	
//	@Override
//	public PeekableIterable<PK> scanKeys(PK startKey, boolean startInclusive, PK end, boolean endInclusive, Config config){
//		return this.backingNode.scanKeys(startKey,startInclusive, end, endInclusive, config);
//	};
//	
//	@Override
//	public PeekableIterable<D> scan(PK startKey, boolean startInclusive, PK end, boolean endInclusive, Config config){
//		return this.backingNode.scan(startKey,startInclusive, end, endInclusive, config);
//	};
//
//	/***************** IndexedStorageReader ************************************/
//
//	@Override
//	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
//		D realObject = this.backingNode.lookupUnique(uniqueKey, config);
//		if( ! CachingMapStorageReaderNode.useCache(config)){
//			return realObject;
//		}
//		if(realObject != null){
//			//TODO add a secondary key cache
//			this.getMapCacheForThisThread().put(realObject.getKey(), realObject);
//		}
//		return realObject;
//	};
//
//	@Override
//	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
//		List<D> realObjects = this.backingNode.lookupMultiUnique(uniqueKeys, config);
//		if( ! CachingMapStorageReaderNode.useCache(config)){
//			return realObjects;
//		}
//		Map<PK,D> mapCacheForThisThread = this.getMapCacheForThisThread();
//		//TODO add a secondary key cache
//		for(D databean : CollectionTool.nullSafe(realObjects)){
//			mapCacheForThisThread.put(databean.getKey(), databean);
//		}
//		return realObjects;
//	};
//	
//	@Override
//	public List<D> lookup(Lookup<PK> lookup, Config config) {
//		if( ! CachingMapStorageReaderNode.useCache(config)){
//			return this.backingNode.lookup(lookup, config);
//		}
//		List<D> fromLookupCache = this.getLookupCacheForThisThread().get(lookup);
//		if(fromLookupCache != null){ return fromLookupCache; }
//		List<D> fromBackingNode = this.backingNode.lookup(lookup, config);
//		Map<PK,D> mapCacheForThisThread = this.getMapCacheForThisThread();
//		for(D databean : CollectionTool.nullSafe(fromBackingNode)){
//			mapCacheForThisThread.put(databean.getKey(), databean);
//		}
//		this.getLookupCacheForThisThread().put(lookup, fromBackingNode);
//		return fromBackingNode;
//	}
//	
//	@Override
//	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config config) {
//		if( ! CachingMapStorageReaderNode.useCache(config)){
//			return this.backingNode.lookup(lookups, config);
//		}
//		Set<Lookup<PK>> lookupMisses = SetTool.createHashSet();
//		Set<D> resultBuilder = SetTool.createHashSet();
//		for(Lookup<PK> lookup : CollectionTool.nullSafe(lookups)){
//			List<D> fromLookupCache = this.getLookupCacheForThisThread().get(lookup);
//			if(fromLookupCache==null){ lookupMisses.add(lookup); }
//			else{ SetTool.nullSafeHashAddAll(resultBuilder, fromLookupCache); }
//		}
//		List<D> fromBackingNode = this.backingNode.lookup(lookupMisses, config);
//		SetTool.nullSafeHashAddAll(resultBuilder, fromBackingNode);
//		List<D> result = ListTool.createArrayList(resultBuilder);
//		Map<PK,D> mapCacheForThisThread = this.getMapCacheForThisThread();
//		for(D databean : CollectionTool.nullSafe(result)){
//			mapCacheForThisThread.put(databean.getKey(), databean);
//		}
//		//can't cache the individual lookups because the result is lumped together
//		
//		//TODO maybe return a List<List<D>> so they can be cached
//		return result;
//	}
	
}
