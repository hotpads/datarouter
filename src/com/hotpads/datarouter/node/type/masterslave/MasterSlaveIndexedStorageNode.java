package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.lookup.Lookup;
import com.hotpads.util.core.CollectionTool;


public class MasterSlaveIndexedStorageNode<D extends Databean,N extends IndexedStorageNode<D>>
extends MasterSlaveIndexedStorageReaderNode<D,N>
implements IndexedStorageNode<D>{
	
	public MasterSlaveIndexedStorageNode(
			Class<D> databeanClass, DataRouter router,
			N master, Collection<N> slaves) {
		
		super(databeanClass, router, master, slaves);
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
	
	
}
