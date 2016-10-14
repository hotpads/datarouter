package com.hotpads.datarouter.storage.view.index.unique;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.KeyIndexEntry;

//basically a marker interface
public interface UniqueKeyIndexEntry<
		IK extends PrimaryKey<IK>,
		IE extends Databean<IK,IE>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends UniqueIndexEntry<IK,IE,PK,D>,
		KeyIndexEntry<IK,IE,PK,D>{


}
