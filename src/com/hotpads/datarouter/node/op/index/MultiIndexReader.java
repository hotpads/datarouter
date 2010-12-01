package com.hotpads.datarouter.node.op.index;

import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface MultiIndexReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		IK extends PrimaryKey<IK>>{

	List<D> lookupMulti(IK indexKey, Config config);
	
}
