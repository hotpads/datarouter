package com.hotpads.datarouter.node.type.index;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.index.MultiIndexReader;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;

public interface ManagedMultiIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>, 
		IK extends PrimaryKey<IK>,
		IE extends MultiIndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK, IE>>
extends MultiIndexReader<PK, D, IK>, ManagedIndexNode<PK, D, IK, IE, IF>{
	
	public static final String
		OP_lookupMultiIndex = "lookupMultiIndex",
		OP_lookupMultiIndexMulti = "lookupMultiIndexMulti",
		OP_scanIndex = "scanIndex";

	List<IE> lookupMultiIndex(IK indexKey, boolean wildcardLastField, Config config);

	List<IE> lookupMultiIndexMulti(Collection<IK> indexKeys, boolean wildcardLastField, Config config);

}
