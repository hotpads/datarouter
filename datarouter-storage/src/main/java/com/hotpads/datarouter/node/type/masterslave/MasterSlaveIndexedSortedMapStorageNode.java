package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;
import java.util.function.Supplier;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.base.BaseMasterSlaveNode;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveIndexedStorageMixin;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveMapStorageMixin;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveSortedStorageMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.java.ReflectionTool;


public class MasterSlaveIndexedSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedSortedMapStorageNode<PK,D>>
extends BaseMasterSlaveNode<PK,D,F,N>
implements MasterSlaveIndexedStorageMixin<PK,D,N>,
		MasterSlaveMapStorageMixin<PK,D,N>,
		MasterSlaveSortedStorageMixin<PK,D,N>,
		IndexedSortedMapStorageNode<PK,D>{

	public MasterSlaveIndexedSortedMapStorageNode(Supplier<D> databeanSupplier, Router router, N master,
			Collection<N> slaves){
		super(databeanSupplier, router, master, slaves);
	}

	/**
	 * @deprecated use {@link #MasterSlaveIndexedSortedMapStorageNode(Supplier, Router, IndexedSortedMapStorageNode,
	 * Collection)}
	 */
	@Deprecated
	public MasterSlaveIndexedSortedMapStorageNode(Class<D> databeanClass, Router router, N master,
			Collection<N> slaves){
		this(ReflectionTool.supplier(databeanClass), router, master, slaves);
	}

}
