package com.hotpads.datarouter.node.type.index;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.index.UniqueIndexReader;
import com.hotpads.datarouter.node.op.index.UniqueIndexWriter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;

public interface ManagedUniqueIndexNode<PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>,
		IK extends PrimaryKey<IK>, 
		IE extends UniqueIndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK, IE>>
		extends UniqueIndexReader<PK, D, IK>, UniqueIndexWriter<PK, D, IK>, ManagedNode<IK, IE, IF>{
	
	public static final String
		OP_lookupUniqueIndex = "lookupUniqueIndex",
		OP_lookupMultiUniqueIndex = "lookupMultiUniqueIndex";

	IE lookupUniqueIndex(IK uniqueKey, Config config);

	List<IE> lookupMultiUniqueIndex(Collection<IK> uniqueKeys, Config config);

}
