package com.hotpads.datarouter.node.op.index;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface UniqueIndexReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		IK extends PrimaryKey<IK>>{

	D lookupUnique(IK indexKey, Config config);
	List<D> lookupMultiUnique( final Collection<IK> uniqueKeys, final Config config);
	
}
