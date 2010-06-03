package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedSortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


public class MasterSlaveIndexedSortedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK>,
N extends IndexedSortedStorageReaderNode<PK,D>>
extends MasterSlaveSortedStorageReaderNode<PK,D,N>
implements IndexedSortedStorageReaderNode<PK,D>{
	
	public MasterSlaveIndexedSortedStorageReaderNode(
			Class<D> databeanClass, DataRouter router,
			N master, Collection<N> slaves) {
		super(databeanClass, router, master, slaves);
	}
	
	public MasterSlaveIndexedSortedStorageReaderNode(
			Class<D> databeanClass, DataRouter router) {
		super(databeanClass, router);
	}

	/***************** IndexedStorageReader ************************************/

	/*
	 * MULTIPLE INHERITANCE... copied from: MasterSlaveIndexedStorageReaderNode
	 */
	@Override
	public List<D> lookup(Lookup<PK> lookup, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.lookup(lookup, config);
	}
	
	/*
	 * MULTIPLE INHERITANCE... copied from: MasterSlaveIndexedStorageReaderNode
	 */
	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.lookup(lookups, config);
	}
	
}
