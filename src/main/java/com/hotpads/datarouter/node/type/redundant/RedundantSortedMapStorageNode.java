package com.hotpads.datarouter.node.type.redundant;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.redundant.mixin.RedundantMapStorageWriterMixin;
import com.hotpads.datarouter.node.type.redundant.mixin.RedundantSortedStorageWriterMixin;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class RedundantSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedMapStorageNode<PK,D>>
extends RedundantSortedMapStorageReaderNode<PK,D,N>
implements SortedMapStorageNode<PK,D>{

	protected RedundantMapStorageWriterMixin<PK,D,N> mixinMapWriteOps;
	protected RedundantSortedStorageWriterMixin<PK,D,N> mixinSortedWriteOps;
	
	public RedundantSortedMapStorageNode(Class<D> databeanClass, Datarouter router,
			Collection<N> writeNodes, N readNode) {
		super(databeanClass, router, writeNodes, readNode);
		this.mixinMapWriteOps = new RedundantMapStorageWriterMixin<PK,D,N>(this);
		this.mixinSortedWriteOps = new RedundantSortedStorageWriterMixin<PK,D,N>(this);
	}

	
	/************************** map ***************************************/

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
	
	
	/**************************** sorted **********************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		mixinSortedWriteOps.deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}
	
	
}
