package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;
import java.util.function.Supplier;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.base.BaseMasterSlaveNode;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveMapStorageMixin;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveSortedStorageMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.java.ReflectionTool;

public class MasterSlaveSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageNode<PK,D>>
extends BaseMasterSlaveNode<PK,D,F,N>
implements MasterSlaveSortedStorageMixin<PK,D,N>,
		MasterSlaveMapStorageMixin<PK,D,N>,
		SortedMapStorageNode<PK,D>{

	public MasterSlaveSortedMapStorageNode(Supplier<D> databeanSupplier, Router router, N master, Collection<N> slaves){
		super(databeanSupplier, router, master, slaves);
	}

	/**
	 * @deprecated use {@link #MasterSlaveSortedMapStorageNode(Supplier, Router, SortedMapStorageNode, Collection)}
	 */
	@Deprecated
	public MasterSlaveSortedMapStorageNode(Class<D> databeanClass, Router router, N master, Collection<N> slaves){
		this(ReflectionTool.supplier(databeanClass), router, master, slaves);
	}

}