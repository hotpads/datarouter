package com.hotpads.datarouter.storage.view.index.unique;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;

//basically a marker interface
public interface UniqueIndexEntry<
		IK extends PrimaryKey<IK>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>> 
extends IndexEntry<IK,PK,D>{

	
}
