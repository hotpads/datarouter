package com.hotpads.datarouter.storage.key.unique.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.base.BaseStringKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseStringUniqueKey<PK extends PrimaryKey<PK>>
extends BaseStringKey<PK>
implements UniqueKey<PK>{

	public BaseStringUniqueKey(String key) {
		super(key);
	}

}
