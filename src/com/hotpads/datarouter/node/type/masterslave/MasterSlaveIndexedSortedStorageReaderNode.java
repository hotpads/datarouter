package com.hotpads.datarouter.node.type.masterslave;

import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedSortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.index.Lookup;

public abstract class MasterSlaveIndexedSortedStorageReaderNode<D extends Databean,N extends IndexedSortedStorageReaderNode<D>>
extends MasterSlaveSortedStorageReaderNode<D,N>
implements IndexedSortedStorageReaderNode<D>{
	
	public MasterSlaveIndexedSortedStorageReaderNode(Class<D> databeanClass, DataRouter router) {
		super(databeanClass, router);
	}

	/***************** IndexedStorageReader ************************************/

	/*
	 * copied code for Multiple Inheritance
	 */
	@Override
	public List<D> lookup(Lookup<D> lookup, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.lookup(lookup, config);
	}
	
}
