package com.hotpads.datarouter.storage.key.unique;


import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface UniqueKey<PK extends PrimaryKey<PK>>
extends Key<PK>{

	PK getPrimaryKey();
	
}
