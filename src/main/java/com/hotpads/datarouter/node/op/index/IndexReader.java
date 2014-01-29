package com.hotpads.datarouter.node.op.index;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexReader<
				PK extends PrimaryKey<PK>,
				D extends Databean<PK,D>,
				IK extends PrimaryKey<IK>>
extends UniqueIndexReader<PK,D,IK>,
		MultiIndexReader<PK,D,IK>{

}
