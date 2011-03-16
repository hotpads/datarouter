package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveMapStorageWriterMixin;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class MasterSlaveMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>//TODO create separate generic type for slaves that is readOnly
extends MasterSlaveMapStorageReaderNode<PK,D,N>
implements MapStorageNode<PK,D>{
	
	protected MasterSlaveMapStorageWriterMixin<PK,D,N> mixinMapWriteOps;
	
	public MasterSlaveMapStorageNode(
			Class<D> databeanClass, DataRouter router,
			N master, Collection<N> slaves) {
		super(databeanClass, router, master, slaves);
		initMixins();
	}

	public MasterSlaveMapStorageNode(
			Class<D> databeanClass, DataRouter router) {
		super(databeanClass, router);
		initMixins();
	}

	protected void initMixins(){
		this.mixinMapWriteOps = new MasterSlaveMapStorageWriterMixin<PK,D,N>(this);
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
