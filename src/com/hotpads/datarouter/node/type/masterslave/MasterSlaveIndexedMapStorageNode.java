package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.IndexedMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveIndexedStorageWriterMixin;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveMapStorageWriterMixin;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


public class MasterSlaveIndexedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		N extends IndexedMapStorageNode<PK,D>>
extends MasterSlaveIndexedMapStorageReaderNode<PK,D,N>
implements IndexedMapStorageNode<PK,D>{

	protected MasterSlaveMapStorageWriterMixin<PK,D,N> mixinMapWriteOps;
	protected MasterSlaveIndexedStorageWriterMixin<PK,D,N> mixinIndexedWriteOps;
	
	public MasterSlaveIndexedMapStorageNode(
			Class<D> databeanClass, DataRouter router,
			N master, Collection<N> slaves) {
		super(databeanClass, router, master, slaves);
		initMixins();
	}
	
	public MasterSlaveIndexedMapStorageNode(
			Class<D> databeanClass, DataRouter router) {
		super(databeanClass, router);
		initMixins();
	}
	
	protected void initMixins(){
		super.initMixins();
		this.mixinMapWriteOps = new MasterSlaveMapStorageWriterMixin<PK,D,N>(this);
		this.mixinIndexedWriteOps = new MasterSlaveIndexedStorageWriterMixin<PK,D,N>(this);
	}
	

	/***************************** MapStorageWriter ****************************/

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
	
	/********************** indexed storage write ops ************************/

	@Override
	public void delete(Lookup<PK> lookup, Config config) {
		mixinIndexedWriteOps.delete(lookup, config);
	}
	
	
	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config) {
		mixinIndexedWriteOps.deleteUnique(uniqueKey, config);
	}

	
	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		mixinIndexedWriteOps.deleteMultiUnique(uniqueKeys, config);
	}
	
	
}
