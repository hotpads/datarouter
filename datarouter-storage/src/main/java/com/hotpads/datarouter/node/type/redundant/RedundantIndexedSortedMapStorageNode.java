package com.hotpads.datarouter.node.type.redundant;

import java.util.Collection;
import java.util.function.Supplier;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.type.redundant.base.BaseRedundantNode;
import com.hotpads.datarouter.node.type.redundant.mixin.RedundantIndexedStorageMixin;
import com.hotpads.datarouter.node.type.redundant.mixin.RedundantMapStorageMixin;
import com.hotpads.datarouter.node.type.redundant.mixin.RedundantSortedStorageMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


public class RedundantIndexedSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedSortedMapStorageNode<PK,D>>
extends BaseRedundantNode<PK,D,N>
implements IndexedSortedMapStorageNode<PK,D>,
		RedundantMapStorageMixin<PK,D,N>,
		RedundantSortedStorageMixin<PK,D,N>,
		RedundantIndexedStorageMixin<PK,D,N>{


	public RedundantIndexedSortedMapStorageNode(Supplier<D> databeanSupplier, Router router,
			Collection<N> writeNodes, N readNode) {
		super(databeanSupplier, router, writeNodes, readNode);
	}

}
