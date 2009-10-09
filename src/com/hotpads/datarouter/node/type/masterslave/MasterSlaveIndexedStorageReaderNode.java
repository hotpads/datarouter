package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.lookup.Lookup;
import com.hotpads.util.core.CollectionTool;


public class MasterSlaveIndexedStorageReaderNode<D extends Databean,N extends IndexedStorageReaderNode<D>>
extends MasterSlaveMapStorageReaderNode<D,N>
implements IndexedStorageReaderNode<D>{
	
	public MasterSlaveIndexedStorageReaderNode(
			Class<D> databeanClass, DataRouter router,
			N master, Collection<N> slaves) {
		
		super(databeanClass, router, master, slaves);
	}
	
	public MasterSlaveIndexedStorageReaderNode(
			Class<D> databeanClass, DataRouter router) {
		
		super(databeanClass, router);
	}

	/***************** IndexedStorageReader ************************************/

	/*
	 * MULTIPLE INHERITANCE... copied to: MasterSlaveIndexedSortedStorageReaderNode
	 */
	@Override
	public List<D> lookup(Lookup<D> lookup, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.lookup(lookup, config);
	}
	
	/*
	 * MULTIPLE INHERITANCE... copied to: MasterSlaveIndexedSortedStorageReaderNode
	 */
	@Override
	public List<D> lookup(Collection<? extends Lookup<D>> lookups, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.lookup(lookups, config);
	}
	
}
