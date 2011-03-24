package com.hotpads.datarouter.node.type.caching.map;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.caching.map.mixin.MapCachingMapStorageWriterMixin;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class MapCachingMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
extends MapCachingMapStorageReaderNode<PK,D,N>
implements MapStorageNode<PK,D>{

	protected MapCachingMapStorageWriterMixin<PK,D,N> mixinMapWriteOps;
	protected boolean cacheWrites = false;

	public MapCachingMapStorageNode(N cacheNode, N backingNode, 
			boolean cacheReads, boolean cacheWrites){
		super(cacheNode, backingNode, cacheReads);
		this.mixinMapWriteOps = new MapCachingMapStorageWriterMixin<PK,D,N>(this, cacheWrites);
		this.cacheWrites = cacheWrites;
	}

	/***************************** MapStorageWriter ****************************/

	@Override
	public void delete(PK key, Config config){
		mixinMapWriteOps.delete(key, config);
	}

	
	@Override
	public void deleteAll(Config config){
		mixinMapWriteOps.deleteAll(config);
	}

	
	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		mixinMapWriteOps.deleteMulti(keys, config);
	}

	
	@Override
	public void put(D databean, Config config){
		mixinMapWriteOps.put(databean, config);
	}

	
	@Override
	public void putMulti(Collection<D> databeans, Config config){
		mixinMapWriteOps.putMulti(databeans, config);
	}

	
	
}
