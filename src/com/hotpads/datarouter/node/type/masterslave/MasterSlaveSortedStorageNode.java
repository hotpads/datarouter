package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.SortedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public class MasterSlaveSortedStorageNode<D extends Databean,
PK extends PrimaryKey<D>,N extends SortedStorageNode<D,PK>>
extends MasterSlaveSortedStorageReaderNode<D,PK,N>
implements SortedStorageNode<D,PK>{
	
	public MasterSlaveSortedStorageNode(
			Class<PK> primaryKeyClass, DataRouter router,
			N master, Collection<N> slaves) {
		super(primaryKeyClass, router, master, slaves);
	}
	
	public MasterSlaveSortedStorageNode(
			Class<PK> primaryKeyClass, DataRouter router) {
		super(primaryKeyClass, router);
	}
	
	/********************** sorted storate write ops ************************/
	
	
	

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

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		this.master.deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}
	
	
}
