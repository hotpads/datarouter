package com.hotpads.datarouter.node.type.redundant;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.SortedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class RedundantSortedStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		N extends SortedStorageNode<PK,D>>
extends RedundantSortedStorageReaderNode<PK,D,N>
implements SortedStorageNode<PK,D>{
	
	public RedundantSortedStorageNode(Class<D> databeanClass, DataRouter router,
			Collection<N> writeNodes, N readNode) {
		super(databeanClass, router, writeNodes, readNode);
	}

	
	/********************** sorted storage write ops ************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		for(N n : this.writeNodes){
			n.deleteRangeWithPrefix(prefix, wildcardLastField, config);
		}
	}
	

	/***************************** MapStorageWriter ****************************/

	/*
	 * MULTIPLE INHERITANCE... copied from RedundantMapStorageNode
	 */


	@Override
	public void delete(PK key, Config config) {
		for(N n : writeNodes){
			n.delete(key, config);
		}
	}

	@Override
	public void deleteAll(Config config) {
		for(N n : writeNodes){
			n.deleteAll(config);
		}
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config) {
		for(N n : writeNodes){
			n.deleteMulti(keys, config);
		}
	}

	@Override
	public void put(D databean, Config config) {
		for(N n : writeNodes){
			n.put(databean, config);
		}
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		for(N n : writeNodes){
			n.putMulti(databeans, config);
		}
	}
	
	
}
