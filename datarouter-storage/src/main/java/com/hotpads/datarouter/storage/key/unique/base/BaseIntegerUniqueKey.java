package com.hotpads.datarouter.storage.key.unique.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.base.BaseIntegerKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseIntegerUniqueKey<K extends Key<K>>
extends BaseIntegerKey<K>
implements UniqueKey<K>{

	public BaseIntegerUniqueKey(Integer key) {
		super(key);
	}

}

