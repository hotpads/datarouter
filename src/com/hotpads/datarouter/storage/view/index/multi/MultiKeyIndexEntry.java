package com.hotpads.datarouter.storage.view.index.multi;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

//basically a marker interface
public interface MultiKeyIndexEntry<
		IK extends PrimaryKey<IK>,
		IE extends Databean<IK,IE>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>> 
extends MultiIndexEntry<IK,IE,PK,D>{

	
}
