package com.hotpads.datarouter.storage.view.index;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface KeyIndexEntry<
		IK extends PrimaryKey<IK>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>> 
extends IndexEntry<IK,PK,D>{

	void fromPrimaryKey(PK targetKey);
	
}
