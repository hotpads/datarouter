package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveIndexedStorageWriterMixin;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveMapStorageWriterMixin;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveSortedStorageWriterMixin;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


public class MasterSlaveIndexedSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedSortedMapStorageNode<PK,D>>
extends MasterSlaveIndexedSortedMapStorageReaderNode<PK,D,F,N>
implements IndexedSortedMapStorageNode<PK,D>{

	protected MasterSlaveMapStorageWriterMixin<PK,D,F,N> mixinMapWriteOps;
	protected MasterSlaveSortedStorageWriterMixin<PK,D,F,N> mixinSortedWriteOps;
	protected MasterSlaveIndexedStorageWriterMixin<PK,D,F,N> mixinIndexedWriteOps;
	
	public MasterSlaveIndexedSortedMapStorageNode(Class<D> databeanClass, DataRouter router, N master,
			Collection<N> slaves){
		super(databeanClass, router, master, slaves);
		initMixins();
	}
	
	public MasterSlaveIndexedSortedMapStorageNode(Class<D> databeanClass, DataRouter router){
		super(databeanClass, router);
		initMixins();
	}
	
	protected void initMixins(){
		super.initMixins();
		this.mixinMapWriteOps = new MasterSlaveMapStorageWriterMixin<PK,D,F,N>(this);
		this.mixinSortedWriteOps = new MasterSlaveSortedStorageWriterMixin<PK,D,F,N>(this);
		this.mixinIndexedWriteOps = new MasterSlaveIndexedStorageWriterMixin<PK,D,F,N>(this);
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
	
	
	/***************************** MapStorageWriter ****************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		mixinSortedWriteOps.deleteRangeWithPrefix(prefix, wildcardLastField, config);
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
