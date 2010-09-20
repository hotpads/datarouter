package com.hotpads.datarouter.node.type.redundant;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class RedundantMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		N extends MapStorageNode<PK,D>>
extends RedundantMapStorageReaderNode<PK,D,N>
implements MapStorageNode<PK,D>{
	
	public RedundantMapStorageNode(Class<D> databeanClass, DataRouter router,
			Collection<N> writeNodes, N readNode) {
		super(databeanClass, router, writeNodes, readNode);
	}
	
	/***************************** MapStorageWriter ****************************/

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
