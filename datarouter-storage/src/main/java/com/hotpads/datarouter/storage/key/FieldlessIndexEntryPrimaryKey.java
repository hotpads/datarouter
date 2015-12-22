package com.hotpads.datarouter.storage.key;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.FieldlessIndexEntry;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface FieldlessIndexEntryPrimaryKey<
		IK extends FieldlessIndexEntryPrimaryKey<IK,PK,D>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends PrimaryKey<IK>{

	PK getTargetKey();
	FieldlessIndexEntry<IK,PK,D> createFromDatabean(D target);

}
