package com.hotpads.datarouter.node.type.masterslave;

import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface MasterSlaveNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends Node<PK,D>>
extends Node<PK,D>{

	public N chooseSlave(Config config);

	@Override
	public N getMaster();

	@Override
	public List<N> getChildNodes();

}
