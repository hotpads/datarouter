package com.hotpads.datarouter.storage.key.primary.base;

import com.hotpads.datarouter.storage.key.base.BaseBooleanKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class BaseBooleanPrimaryKey<PK extends PrimaryKey<PK>>extends BaseBooleanKey<PK> implements
PrimaryKey<PK>{

	public BaseBooleanPrimaryKey(Boolean key){
		super(key);
	}

	@Override
	public PrimaryKey<PK> getPrimaryKey(){
		return this;
	}

}
