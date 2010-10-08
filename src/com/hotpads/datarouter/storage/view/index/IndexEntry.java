package com.hotpads.datarouter.storage.view.index;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexEntry<IK extends PrimaryKey<IK>,TK extends PrimaryKey<TK>> 
extends Databean<IK>{

	TK getTargetKey();
	
}
