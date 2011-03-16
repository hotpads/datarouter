package com.hotpads.datarouter.storage.key.unique.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.base.BaseStringKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseStringUniqueKey<K extends Key<K>>
extends BaseStringKey<K>
implements UniqueKey<K>{

	public BaseStringUniqueKey(String key) {
		super(key);
	}

}
