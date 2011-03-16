package com.hotpads.datarouter.storage.key.unique.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.base.BaseBooleanKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseBooleanUniqueKey<K extends Key<K>>
extends BaseBooleanKey<K>
implements UniqueKey<K>{

	public BaseBooleanUniqueKey(Boolean key) {
		super(key);
	}

}
