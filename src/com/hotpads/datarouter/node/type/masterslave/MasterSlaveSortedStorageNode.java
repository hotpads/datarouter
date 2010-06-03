package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.SortedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public class MasterSlaveSortedStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>,
		N extends SortedStorageNode<PK,D>>
extends MasterSlaveSortedStorageReaderNode<PK,D,N>
implements SortedStorageNode<PK,D>{
	
	public MasterSlaveSortedStorageNode(
			Class<D> databeanClass, DataRouter router,
			N master, Collection<N> slaves) {
		super(databeanClass, router, master, slaves);
	}
	
	public MasterSlaveSortedStorageNode(
			Class<D> databeanClass, DataRouter router) {
		super(databeanClass, router);
	}
	
	/********************** sorted storate write ops ************************/
	
	
	

	/***************************** MapStorageWriter ****************************/

	/*
	 * MULTIPLE INHERITANCE... copied from MasterSlaveMapStorageNode
	 */

	@Override
	public void delete(UniqueKey<PK> key, Config config) {
		this.master.delete(key, config);
	}

	@Override
	public void deleteAll(Config config) {
		this.master.deleteAll(config);
	}

	@Override
	public void deleteMulti(Collection<? extends UniqueKey<PK>> keys, Config config) {
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
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		this.master.deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}
	
	
}
