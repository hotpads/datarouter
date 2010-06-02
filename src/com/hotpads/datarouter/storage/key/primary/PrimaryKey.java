package com.hotpads.datarouter.storage.key.primary;


import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public interface PrimaryKey<PK extends PrimaryKey<PK>>
extends UniqueKey<PK>{

	PrimaryKey<PK> getPrimaryKey();
	
}
