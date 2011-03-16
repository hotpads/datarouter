package com.hotpads.datarouter.node.type.caching;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.IndexedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.type.caching.mixin.CachingMapStorageWriterMixin;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class CachingMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedMapStorageNode<PK,D>>
extends CachingMapStorageReaderNode<PK,D,N>
implements MapStorageWriter<PK,D>{

	protected CachingMapStorageWriterMixin<PK,D,N> mixinMapWriteOps;

	public CachingMapStorageNode(N backingNode) {
		super(backingNode);
		this.mixinMapWriteOps = new CachingMapStorageWriterMixin<PK,D,N>(this);
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
