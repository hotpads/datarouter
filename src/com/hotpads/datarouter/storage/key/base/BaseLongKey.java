package com.hotpads.datarouter.storage.key.base;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.BaseKey;

@SuppressWarnings("serial")
public abstract class BaseLongKey<D extends Databean>
extends BaseKey<D>{

	protected Long key;
	
	public BaseLongKey(Class<D> databeanClass, Long key) {
		super(databeanClass);
		this.key = key;
	}

	public Long getKey() {
		return key;
	}

	
	
}
