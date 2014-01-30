package com.hotpads.datarouter.node.type.redundant;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter.MapStorageWriterNode;
import com.hotpads.datarouter.node.type.redundant.mixin.RedundantMapStorageWriterMixin;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class RedundantMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
extends RedundantMapStorageReaderNode<PK,D,N>
implements MapStorageWriterNode<PK,D>{

	protected RedundantMapStorageWriterMixin<PK,D,N> mixinMapWriteOps;

	public RedundantMapStorageNode(Class<D> databeanClass, DataRouter router,
			Collection<N> writeNodes, N readNode) {
		super(databeanClass, router, writeNodes, readNode);
		this.mixinMapWriteOps = new RedundantMapStorageWriterMixin<PK,D,N>(this);
	}
	
	
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
