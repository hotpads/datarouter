package com.hotpads.datarouter.node.type.indexing;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.indexing.BaseIndexingNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter.MapStorageWriterNode;
import com.hotpads.datarouter.node.type.indexing.mixin.IndexingMapStorageWriterMixin;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class IndexingMapStorageWriterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		N extends MapStorageNode<PK,D>>
extends BaseIndexingNode<PK,D,N>
implements MapStorageWriterNode<PK,D>{
	
	protected IndexingMapStorageWriterMixin<PK,D,N> mixinMapWriteOps;
	
	public IndexingMapStorageWriterNode(N mainNode) {
		super(mainNode);
		initMixins();
	}

	protected void initMixins(){
		this.mixinMapWriteOps = new IndexingMapStorageWriterMixin<PK,D,N>(mainNode, indexListeners);
	}
	
	
	@Override
	public void delete(PK key, Config config) {
		mixinMapWriteOps.delete(key, config);
	}

	@Override
	public void deleteAll(Config config) {
		mixinMapWriteOps.deleteAll(config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config) {
		mixinMapWriteOps.deleteMulti(keys, config);
	}

	@Override
	public void put(D databean, Config config) {
		mixinMapWriteOps.put(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		mixinMapWriteOps.putMulti(databeans, config);
	}
	
}
