package com.hotpads.datarouter.node.type.index;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.index.MultiIndexReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;

public interface ManagedMultiIndexNode<PK extends PrimaryKey<PK>, D extends Databean<PK, D>, IK extends PrimaryKey<IK>,
	IE extends MultiIndexEntry<IK, IE, PK, D>>
		extends MultiIndexReader<PK, D, IK>{
	
	public static final String
		OP_lookupMultiIndex = "lookupMultiIndex",
		OP_lookupMultiIndexMulti = "lookupMultiIndexMulti";

	List<IE> lookupMultiIndex(IK indexKey, boolean wildcardLastField, Config config);

	List<IE> lookupMultiIndexMulti(Collection<IK> indexKeys, boolean wildcardLastField, Config config);

}
