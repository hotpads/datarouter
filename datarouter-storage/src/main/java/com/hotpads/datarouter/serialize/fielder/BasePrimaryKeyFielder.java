package com.hotpads.datarouter.serialize.fielder;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class BasePrimaryKeyFielder<PK extends PrimaryKey<PK>>
implements PrimaryKeyFielder<PK>{

	@Override
	public boolean isEntity(){
		return false;
	}
	
}
