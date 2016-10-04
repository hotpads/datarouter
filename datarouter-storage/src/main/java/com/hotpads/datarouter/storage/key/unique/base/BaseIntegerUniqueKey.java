package com.hotpads.datarouter.storage.key.unique.base;


import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.base.BaseIntegerKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


public abstract class BaseIntegerUniqueKey<K extends Key<K>>
extends BaseIntegerKey<K>
implements UniqueKey<K>{

	public BaseIntegerUniqueKey(Integer key) {
		super(key);
	}

}

