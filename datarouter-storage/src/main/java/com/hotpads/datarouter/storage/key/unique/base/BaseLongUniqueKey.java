package com.hotpads.datarouter.storage.key.unique.base;


import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.base.BaseLongKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


@SuppressWarnings("serial")
public abstract class BaseLongUniqueKey<K extends Key<K>>
extends BaseLongKey<K>
implements UniqueKey<K>{

	public BaseLongUniqueKey(Long key) {
		super(key);
	}

}

