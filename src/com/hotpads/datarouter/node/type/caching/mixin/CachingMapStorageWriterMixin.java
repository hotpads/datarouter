package com.hotpads.datarouter.node.type.caching.mixin;

import java.util.Collection;
import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.caching.BaseCachingNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter.MapStorageWriterNode;
import com.hotpads.datarouter.node.type.caching.CachingMapStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;

public class CachingMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		N extends MapStorageWriterNode<PK,D>>
implements MapStorageWriter<PK,D>{
	
	protected BaseCachingNode<PK,D,N> target;
	
	public CachingMapStorageWriterMixin(BaseCachingNode<PK,D,N> target){
		this.target = target;
	}

	@Override
	public void delete(PK key, Config config) {
		target.getMapCacheForThisThread().remove(key);
		target.getBackingNode().delete(key, config);
	}

	@Override
	public void deleteAll(Config config) {
		target.getMapCacheForThisThread().clear();
		target.getBackingNode().deleteAll(config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config) {
		for(Key<PK> key : CollectionTool.nullSafe(keys)){
			target.getMapCacheForThisThread().remove(key);
		}
		target.getBackingNode().deleteMulti(keys, config);
	}

	@Override
	public void put(D databean, Config config) {
		if(databean==null || databean.getKey()==null){ return; }
		target.getBackingNode().put(databean, config);
		if(CachingMapStorageReaderNode.useCache(config)){
			target.getMapCacheForThisThread().put(databean.getKey(), databean);
		}
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		target.getBackingNode().putMulti(databeans, config);
		Map<PK,D> cacheForThisThread = target.getMapCacheForThisThread();
		for(D databean : CollectionTool.nullSafe(databeans)){
			cacheForThisThread.put(databean.getKey(), databean);
		}
	}
	
}
