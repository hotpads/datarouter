package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


public class MasterSlaveIndexedStorageNode<PK extends PrimaryKey<PK>,
											D extends Databean<PK>,
											N extends IndexedStorageNode<PK,D>>
extends MasterSlaveIndexedStorageReaderNode<PK,D,N>
implements IndexedStorageNode<PK,D>{
	
	public MasterSlaveIndexedStorageNode(
			Class<D> databeanClass, DataRouter router,
			N master, Collection<N> slaves) {
		super(databeanClass, router, master, slaves);
	}
	
	public MasterSlaveIndexedStorageNode(
			Class<D> databeanClass, DataRouter router) {
		super(databeanClass, router);
	}
	
	/********************** indexed storage write ops ************************/

	/*
	 * MULTIPLE INHERITANCE... copied to:
	 *   - MasterSlaveIndexedSortedStorageNode
	 */
	
	@Override
	public void delete(Lookup<PK> lookup, Config config) {
		this.master.delete(lookup, config);
	}
	
	/*
	 * MULTIPLE INHERITANCE... copied from MasterSlaveIndexedSortedStorageNode
	 */
	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config) {
		this.master.deleteUnique(uniqueKey, config);
	}

	/*
	 * MULTIPLE INHERITANCE... copied from MasterSlaveMapStorageNode
	 */
	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		this.master.deleteMultiUnique(uniqueKeys, config);
	}
	

	/***************************** MapStorageWriter ****************************/

	/*
	 * MULTIPLE INHERITANCE... copied from MasterSlaveMapStorageNode
	 */
	@Override
	public void delete(PK key, Config config) {
		this.master.delete(key, config);
	}

	/*
	 * MULTIPLE INHERITANCE... copied from MasterSlaveMapStorageNode
	 */
	@Override
	public void deleteAll(Config config) {
		this.master.deleteAll(config);
	}

	/*
	 * MULTIPLE INHERITANCE... copied from MasterSlaveMapStorageNode
	 */
	@Override
	public void deleteMulti(Collection<PK> keys, Config config) {
		this.master.deleteMulti(keys, config);
	}

	/*
	 * MULTIPLE INHERITANCE... copied from MasterSlaveMapStorageNode
	 */
	@Override
	public void put(D databean, Config config) {
		this.master.put(databean, config);
	}

	/*
	 * MULTIPLE INHERITANCE... copied from MasterSlaveMapStorageNode
	 */
	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		this.master.putMulti(databeans, config);
	}
	
	
}
