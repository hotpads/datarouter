package com.hotpads.datarouter.storage.key.multi;

import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface Lookup<PK extends PrimaryKey<PK>>
extends Key<PK>{
	
	void setPrimaryKey(PK primaryKey);
	PK getPrimaryKey();
	
}
