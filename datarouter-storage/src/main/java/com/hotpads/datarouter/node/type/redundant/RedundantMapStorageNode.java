package com.hotpads.datarouter.node.type.redundant;

import java.util.Collection;
import java.util.function.Supplier;

import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.redundant.base.BaseRedundantNode;
import com.hotpads.datarouter.node.type.redundant.mixin.RedundantMapStorageMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class RedundantMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
extends BaseRedundantNode<PK,D,N>
implements MapStorageNode<PK,D>,
		RedundantMapStorageMixin<PK,D,N>{

	public RedundantMapStorageNode(Supplier<D> databeanSupplier, Router router,
			Collection<N> writeNodes, N readNode) {
		super(databeanSupplier, router, writeNodes, readNode);
	}

}