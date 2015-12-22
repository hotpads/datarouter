package com.hotpads.datarouter.storage.lazy.base;

import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface LazyReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,S extends NodeOps<PK,D>>{

	public S getBackingStorage();

}
