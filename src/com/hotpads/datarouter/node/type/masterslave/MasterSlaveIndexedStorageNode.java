package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;


public class MasterSlaveIndexedStorageNode<D extends Databean,
PK extends PrimaryKey<D>,N extends IndexedStorageNode<D,PK>>
extends MasterSlaveIndexedStorageReaderNode<D,PK,N>
implements IndexedStorageNode<D,PK>{
	
	public MasterSlaveIndexedStorageNode(
			Class<PK> primaryKeyClass, DataRouter router,
			N master, Collection<N> slaves) {
		super(primaryKeyClass, router, master, slaves);
	}
	
	public MasterSlaveIndexedStorageNode(
			Class<PK> primaryKeyClass, DataRouter router) {
		super(primaryKeyClass, router);
	}
	
	/********************** indexed storage write ops ************************/

	/*
	 * MULTIPLE INHERITANCE... copied to:
	 *   - MasterSlaveIndexedSortedStorageNode
	 */
	
	@Override
	public void delete(Lookup<D> lookup, Config config) {
		this.master.delete(lookup, config);
	}
	

	/***************************** MapStorageWriter ****************************/

	/*
	 * MULTIPLE INHERITANCE... copied from MasterSlaveMapStorageNode
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
