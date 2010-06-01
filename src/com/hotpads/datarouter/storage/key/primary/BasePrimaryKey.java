package com.hotpads.datarouter.storage.key.primary;

import com.hotpads.datarouter.storage.key.unique.BaseUniqueKey;

@SuppressWarnings("serial")
public abstract class BasePrimaryKey<PK extends PrimaryKey<PK>>
extends BaseUniqueKey<PK> 
implements PrimaryKey<PK>{

	public BasePrimaryKey(){
		super();
	}
	
}
