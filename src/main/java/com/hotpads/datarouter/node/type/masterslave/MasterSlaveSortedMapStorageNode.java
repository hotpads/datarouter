package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveMapStorageWriterMixin;
import com.hotpads.datarouter.node.type.masterslave.mixin.MasterSlaveSortedStorageWriterMixin;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class MasterSlaveSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageNode<PK,D>>
extends MasterSlaveSortedMapStorageReaderNode<PK,D,F,N>
implements SortedMapStorageNode<PK,D>{

	protected MasterSlaveMapStorageWriterMixin<PK,D,F,N> mixinMapWriteOps;
	protected MasterSlaveSortedStorageWriterMixin<PK,D,F,N> mixinSortedWriteOps;
	
	public MasterSlaveSortedMapStorageNode(Class<D> databeanClass, Datarouter router,
			N master, Collection<N> slaves) {
		super(databeanClass, router, master, slaves);
		initMixins();
	}
	
	public MasterSlaveSortedMapStorageNode(Class<D> databeanClass, Datarouter router){
		super(databeanClass, router);
		initMixins();
	}
	
	protected void initMixins(){
		this.mixinMapWriteOps = new MasterSlaveMapStorageWriterMixin<PK,D,F,N>(this);
		this.mixinSortedWriteOps = new MasterSlaveSortedStorageWriterMixin<PK,D,F,N>(this);
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

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		mixinSortedWriteOps.deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}
	
	
}
