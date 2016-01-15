package com.hotpads.datarouter.node.type.index;

import com.hotpads.datarouter.node.op.index.MultiIndexReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;

public interface MultiIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>,
		IK extends PrimaryKey<IK>,
		IE extends MultiIndexEntry<IK, IE, PK, D>>
extends MultiIndexReader<PK, D, IK, IE>{

}
