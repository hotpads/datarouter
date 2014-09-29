package com.hotpads.datarouter.node.op.index;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface UniqueIndexWriter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>>{
	
	public static final String
		OP_deleteUnique = "deleteUnique",
		OP_deleteMultiUnique = "deleteMultiUnique";
	
	void deleteUnique(IK indexKey, Config config);
	void deleteMultiUnique(Collection<IK> uniqueKeys, Config config);
}
