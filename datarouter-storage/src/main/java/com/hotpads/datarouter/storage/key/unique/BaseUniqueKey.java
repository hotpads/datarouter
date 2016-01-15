package com.hotpads.datarouter.storage.key.unique;

import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.datarouter.storage.key.Key;

@SuppressWarnings("serial")
public abstract class BaseUniqueKey<K extends Key<K>>
extends BaseKey<K> 
implements UniqueKey<K>{
	
}
