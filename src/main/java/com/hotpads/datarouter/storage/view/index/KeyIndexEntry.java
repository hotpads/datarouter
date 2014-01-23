package com.hotpads.datarouter.storage.view.index;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface KeyIndexEntry<
		IK extends PrimaryKey<IK>,
		IE extends Databean<IK,IE>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>> 
extends IndexEntry<IK,IE,PK,D>{

	void fromPrimaryKey(PK targetKey);
	
}
