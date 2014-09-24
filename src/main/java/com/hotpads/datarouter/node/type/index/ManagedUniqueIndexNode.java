package com.hotpads.datarouter.node.type.index;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.index.UniqueIndexReader;
import com.hotpads.datarouter.node.op.index.UniqueIndexWriter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;

public interface ManagedUniqueIndexNode<PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>,
		IK extends PrimaryKey<IK>, 
		IE extends UniqueIndexEntry<IK, IE, PK, D>>
		extends UniqueIndexReader<PK, D, IK>, UniqueIndexWriter<PK, D, IK>{

	IE lookupIndex(IK uniqueKey, Config config);

	List<IE> lookupMultiIndex(Collection<IK> uniqueKeys, Config config);

}
