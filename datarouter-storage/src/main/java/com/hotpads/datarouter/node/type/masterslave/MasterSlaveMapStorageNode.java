package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;
import java.util.function.Supplier;

import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.base.BaseMasterSlaveNode;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveMapStorageMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.java.ReflectionTool;

public class MasterSlaveMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageNode<PK,D>>//TODO create separate generic type for slaves that is readOnly
extends BaseMasterSlaveNode<PK,D,F,N>
implements MasterSlaveMapStorageMixin<PK,D,N>, MapStorageNode<PK,D>{

	public MasterSlaveMapStorageNode(Supplier<D> databeanSupplier, Router router, N master, Collection<N> slaves){
		super(databeanSupplier, router, master, slaves);
	}

	/**
	 * @deprecated use {@link #MasterSlaveMapStorageNode(Supplier, Router, MapStorageNode, Collection)}
	 */
	@Deprecated
	public MasterSlaveMapStorageNode(Class<D> databeanClass, Router router, N master, Collection<N> slaves){
		this(ReflectionTool.supplier(databeanClass), router, master, slaves);
	}
}
