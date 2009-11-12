package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedSortedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.lookup.Lookup;


public class MasterSlaveIndexedSortedStorageNode<D extends Databean,N extends IndexedSortedStorageNode<D>>
extends MasterSlaveIndexedSortedStorageReaderNode<D,N>
implements IndexedSortedStorageNode<D>{
	
	public MasterSlaveIndexedSortedStorageNode(
			Class<D> databeanClass, DataRouter router,
			N master, Collection<N> slaves) {
		
		super(databeanClass, router, master, slaves);
	}
	
	public MasterSlaveIndexedSortedStorageNode(
			Class<D> databeanClass, DataRouter router) {
		
		super(databeanClass, router);
	}
	

	
	/********************** indexed storage write ops ************************/

	/*
	 * MULTIPLE INHERITANCE... copied from MasterSlaveIndexedStorageNode
	 */
	
	@Override
	public void delete(Lookup<D> lookup, Config config) {
		this.master.delete(lookup, config);
	}
	

	/***************************** MapStorageWriter ****************************/

	/*
	 * MULTIPLE INHERITANCE... copied to:
	 *   - MasterSlaveSortedStorageNode
	 *   - MasterSlaveIndexedStorageNode
	 */

	@Override
	public void delete(Key<D> key, Config config) {
		this.master.delete(key, config);
	}

	@Override
	public void deleteAll(Config config) {
		this.master.deleteAll(config);
	}

	@Override
	public void deleteMulti(Collection<? extends Key<D>> keys, Config config) {
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

	@Override
	public void deleteRangeWithPrefix(Key<D> prefix, boolean wildcardLastField, Config config) {
		this.master.deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}
	
	
	
}
