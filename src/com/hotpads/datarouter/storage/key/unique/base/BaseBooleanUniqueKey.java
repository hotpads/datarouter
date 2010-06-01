package com.hotpads.datarouter.storage.key.unique.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.base.BaseBooleanKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseBooleanUniqueKey<PK extends PrimaryKey<PK>>
extends BaseBooleanKey<PK>
implements UniqueKey<PK>{

	public BaseBooleanUniqueKey(Boolean key) {
		super(key);
	}

}
