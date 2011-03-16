package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.IndexedMapStorageReader.IndexedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader.IndexedStorageReaderNode;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveIndexedStorageReaderMixin;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


public class MasterSlaveIndexedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedMapStorageReaderNode<PK,D>>
extends MasterSlaveMapStorageReaderNode<PK,D,N>
implements IndexedStorageReaderNode<PK,D>{

	protected MasterSlaveIndexedStorageReaderMixin<PK,D,N> mixinIndexedReadOps;

	public MasterSlaveIndexedMapStorageReaderNode(
			Class<D> databeanClass, DataRouter router,
			N master, Collection<N> slaves) {
		super(databeanClass, router, master, slaves);
		initMixins();
	}
	
	public MasterSlaveIndexedMapStorageReaderNode(
			Class<D> databeanClass, DataRouter router) {
		super(databeanClass, router);
		initMixins();
	}
	
	protected void initMixins(){
		this.mixinIndexedReadOps = new MasterSlaveIndexedStorageReaderMixin<PK,D,N>(this);
	}

	/***************** IndexedStorageReader ************************************/

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		return mixinIndexedReadOps.lookupUnique(uniqueKey, config);
	}
	
	
	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		return mixinIndexedReadOps.lookupMultiUnique(uniqueKeys, config);
	}

	
	@Override
	public List<D> lookup(Lookup<PK> lookup, Config config) {
		return mixinIndexedReadOps.lookup(lookup, config);
	}
	
	
	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config config) {
		return mixinIndexedReadOps.lookup(lookups, config);
	}
	
}
