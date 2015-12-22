package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface NodeOps<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{
}
