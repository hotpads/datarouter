package com.hotpads.datarouter.storage.key.primary.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.base.BaseBooleanKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseBooleanPrimaryKey<PK extends PrimaryKey<PK>>
extends BaseBooleanKey<PK>
implements PrimaryKey<PK>{

	public BaseBooleanPrimaryKey(Boolean key) {
		super(key);
	}
	
	@Override
	public PrimaryKey<PK> getPrimaryKey(){
		return this;
	}

}
