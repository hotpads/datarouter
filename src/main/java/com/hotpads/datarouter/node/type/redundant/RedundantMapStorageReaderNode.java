package com.hotpads.datarouter.node.type.redundant;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.node.type.redundant.base.BaseRedundantNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class RedundantMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageReaderNode<PK,D>>
extends BaseRedundantNode<PK,D,N>
implements MapStorageReaderNode<PK,D>{
	
	public RedundantMapStorageReaderNode(Class<D> databeanClass, Datarouter router,
			Collection<N> writeNodes, N readNode) {
		super(databeanClass, router, writeNodes, readNode);
	}

	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(PK key, Config config){
		return readNode.exists(key, config);
	}

	@Override
	public D get(PK key, Config config) {
		return readNode.get(key, config);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config) {
		return readNode.getMulti(keys, config);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config) {
		return readNode.getKeys(keys, config);
	}

	
	
}
