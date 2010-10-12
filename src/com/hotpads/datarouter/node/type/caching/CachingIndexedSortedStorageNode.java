package com.hotpads.datarouter.node.type.caching;

import com.hotpads.datarouter.node.op.combo.IndexedMapStorage;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class CachingIndexedSortedStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		N extends IndexedMapStorage<PK,D>>
//extends CachingIndexedSortedStorageReaderNode<PK,D,N>
//implements IndexedMapStorage<PK,D>
{
//
//	
//	public CachingIndexedSortedStorageNode(N backingNode) {
//		super(backingNode);
//	}
//
//	/********************** indexed storage write ops ************************/
//
//	/*
//	 * MULTIPLE INHERITANCE
//	 */
//	
//	@Override
//	public void delete(Lookup<PK> lookup, Config config) {
//		this.clearNonMapCaches();
//		this.backingNode.delete(lookup, config);
//	}
//
//	@Override
//	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config) {
//		this.clearNonMapCaches();
//		this.backingNode.deleteUnique(uniqueKey, config);
//	}
//
//	@Override
//	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
//		this.clearNonMapCaches();
//		this.backingNode.deleteMultiUnique(uniqueKeys, config);
//	}
//
//	/***************************** MapStorageWriter ****************************/
//
//	/*
//	 * MULTIPLE INHERITANCE
//	 */
//
//	@Override
//	public void delete(PK key, Config config) {
//		this.clearNonMapCaches();
//		this.getMapCacheForThisThread().remove(key);
//		this.backingNode.delete(key, config);
//	}
//
//	@Override
//	public void deleteAll(Config config) {
//		this.clearNonMapCaches();
//		this.getMapCacheForThisThread().clear();
//		this.backingNode.deleteAll(config);
//	}
//
//	@Override
//	public void deleteMulti(Collection<PK> keys, Config config) {
//		this.clearNonMapCaches();
//		Map<PK,D> mapCacheForThisThread = this.getMapCacheForThisThread();
//		for(Key<PK> key : CollectionTool.nullSafe(keys)){
//			mapCacheForThisThread.remove(key);
//		}
//		this.backingNode.deleteMulti(keys, config);
//	}
//
//	@Override
//	public void put(D databean, Config config) {
//		this.clearNonMapCaches();
//		if(databean==null || databean.getKey()==null){ return; }
//		this.backingNode.put(databean, config);
//		if(CachingMapStorageReaderNode.useCache(config)){
//			this.getMapCacheForThisThread().put(databean.getKey(), databean);
//		}
//	}
//
//	@Override
//	public void putMulti(Collection<D> databeans, Config config) {
//		this.clearNonMapCaches();
//		this.backingNode.putMulti(databeans, config);
//		Map<PK,D> mapCacheForThisThread = this.getMapCacheForThisThread();
//		for(D databean : CollectionTool.nullSafe(databeans)){
//			mapCacheForThisThread.put(databean.getKey(), databean);
//		}
//	}
//
//	@Override
//	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField,
//			Config config) {
//		this.getMapCacheForThisThread().clear();
//		this.clearNonMapCaches();
//		this.backingNode.deleteRangeWithPrefix(prefix, wildcardLastField, config);
//	}
//	
//	
}
