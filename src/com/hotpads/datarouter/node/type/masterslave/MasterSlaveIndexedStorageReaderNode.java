package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.masterslave.BaseMasterSlaveNode;
import com.hotpads.datarouter.node.op.IndexedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.index.Lookup;
import com.hotpads.datarouter.storage.key.Key;

public abstract class MasterSlaveIndexedStorageReaderNode<D extends Databean,N extends IndexedStorageReaderNode<D>>
extends BaseMasterSlaveNode<D,N>
implements IndexedStorageReaderNode<D>{
	
	public MasterSlaveIndexedStorageReaderNode(Class<D> databeanClass, DataRouter router) {
		super(databeanClass, router);
	}

	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(Key<D> key, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.exists(key, config);
	}

	@Override
	public D get(Key<D> key, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.get(key, config);
	}

	@Override
	public List<D> getAll(Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getAll(config);
	}

	@Override
	public List<D> getMulti(Collection<? extends Key<D>> keys, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getMulti(keys, config);
	}



	/***************** IndexedStorageReader ************************************/
	
	@Override
	public List<D> lookup(Lookup<D> lookup, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.lookup(lookup, config);
	}
	
}
