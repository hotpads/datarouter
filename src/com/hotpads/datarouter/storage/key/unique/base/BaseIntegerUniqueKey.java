package com.hotpads.datarouter.storage.key.unique.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.base.BaseIntegerKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseIntegerUniqueKey<PK extends PrimaryKey<PK>>
extends BaseIntegerKey<PK>
implements UniqueKey<PK>{

	public BaseIntegerUniqueKey(Integer key) {
		super(key);
	}

}

