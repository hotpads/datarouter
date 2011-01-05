package com.hotpads.datarouter.storage.view.index.multi;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.KeyIndexEntry;

//basically a marker interface
public interface MultiKeyIndexEntry<
		IK extends PrimaryKey<IK>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>> 
extends MultiIndexEntry<IK,PK,D>,
		KeyIndexEntry<IK,PK,D>{

	
}
