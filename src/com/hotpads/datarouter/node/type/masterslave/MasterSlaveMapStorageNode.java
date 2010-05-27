package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public class MasterSlaveMapStorageNode<D extends Databean,
PK extends PrimaryKey<D>,N extends MapStorageNode<D,PK>>
extends MasterSlaveMapStorageReaderNode<D,PK,N>
implements MapStorageNode<D,PK>{
	
	public MasterSlaveMapStorageNode(
			Class<PK> primaryKeyClass, DataRouter router,
			N master, Collection<N> slaves) {
		super(primaryKeyClass, router, master, slaves);
	}

	public MasterSlaveMapStorageNode(
			Class<PK> primaryKeyClass, DataRouter router) {
		super(primaryKeyClass, router);
	}
	
	/***************************** MapStorageWriter ****************************/

	/*
	 * MULTIPLE INHERITANCE... copied to:
	 *   - MasterSlaveSortedStorageNode
	 *   - MasterSlaveIndexedStorageNode
	 */

	@Override
	public void delete(UniqueKey<D> key, Config config) {
		this.master.delete(key, config);
	}

	@Override
	public void deleteAll(Config config) {
		this.master.deleteAll(config);
	}

	@Override
	public void deleteMulti(Collection<? extends UniqueKey<D>> keys, Config config) {
		this.master.deleteMulti(keys, config);
	}

	@Override
	public void put(D databean, Config config) {
		this.master.put(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		this.master.putMulti(databeans, config);
	}
	
	
}
