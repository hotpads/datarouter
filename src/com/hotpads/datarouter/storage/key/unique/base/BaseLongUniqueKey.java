package com.hotpads.datarouter.storage.key.unique.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.base.BaseLongKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseLongUniqueKey<PK extends PrimaryKey<PK>>
extends BaseLongKey<PK>
implements UniqueKey<PK>{

	public BaseLongUniqueKey(Long key) {
		super(key);
	}

}

