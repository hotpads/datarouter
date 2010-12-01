package com.hotpads.datarouter.storage.view.index;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public interface IndexEntry<
		IK extends PrimaryKey<IK>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>> 
extends Databean<IK>, UniqueKey<PK>{

	PK getTargetKey();
	void fromDatabean(D target);
	
}
