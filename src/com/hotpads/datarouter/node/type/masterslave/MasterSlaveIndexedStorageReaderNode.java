package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;


public class MasterSlaveIndexedStorageReaderNode<D extends Databean,
PK extends PrimaryKey<D>,N extends IndexedStorageReaderNode<D,PK>>
extends MasterSlaveMapStorageReaderNode<D,PK,N>
implements IndexedStorageReaderNode<D,PK>{
	
	public MasterSlaveIndexedStorageReaderNode(
			Class<PK> primaryKeyClass, DataRouter router,
			N master, Collection<N> slaves) {
		super(primaryKeyClass, router, master, slaves);
	}
	
	public MasterSlaveIndexedStorageReaderNode(
			Class<PK> primaryKeyClass, DataRouter router) {
		super(primaryKeyClass, router);
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
