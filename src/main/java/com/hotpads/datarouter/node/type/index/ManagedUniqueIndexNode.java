package com.hotpads.datarouter.node.type.index;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;

public interface ManagedUniqueIndexNode<PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>,
		IK extends PrimaryKey<IK>, 
		IE extends UniqueIndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK, IE>>
		extends UniqueIndexNode<PK, D, IK, IE>, ManagedNode<IK, IE, IF>{
	
	public static final String
		OP_lookupUniqueIndex = "lookupUniqueIndex",
		OP_lookupMultiUniqueIndex = "lookupMultiUniqueIndex";

}
