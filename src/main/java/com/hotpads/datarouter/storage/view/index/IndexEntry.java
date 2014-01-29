package com.hotpads.datarouter.storage.view.index;

import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexEntry<
		IK extends PrimaryKey<IK>,
		IE extends Databean<IK,IE>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>> 
extends Databean<IK,IE>{

	PK getTargetKey();
//	void fromDatabean(D target);
	List<IE> createFromDatabean(D target);
	
}
