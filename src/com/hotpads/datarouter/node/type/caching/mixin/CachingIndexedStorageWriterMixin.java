package com.hotpads.datarouter.node.type.caching.mixin;

import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter.IndexedStorageWriterNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class CachingIndexedStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedStorageWriterNode<PK,D>>
//implements IndexedStorageWriter<PK,D>
{
//	
//	protected BaseCachingNode<PK,D,N> target;
//	
//	public CachingIndexedStorageWriterMixin(BaseCachingNode<PK,D,N> target){
//		this.target = target;
//	}
//
//	@Override
//	public void delete(Lookup<PK> lookup, Config config) {
//		target.clearNonMapCaches();
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
}
