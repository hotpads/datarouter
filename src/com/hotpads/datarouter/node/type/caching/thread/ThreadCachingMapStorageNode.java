package com.hotpads.datarouter.node.type.caching.thread;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.IndexedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.type.caching.thread.mixin.ThreadCachingMapStorageWriterMixin;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class ThreadCachingMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedMapStorageNode<PK,D>>
extends ThreadCachingMapStorageReaderNode<PK,D,N>
implements MapStorageWriter<PK,D>{

	protected ThreadCachingMapStorageWriterMixin<PK,D,N> mixinMapWriteOps;

	public ThreadCachingMapStorageNode(N backingNode) {
		super(backingNode);
		this.mixinMapWriteOps = new ThreadCachingMapStorageWriterMixin<PK,D,N>(this);
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
