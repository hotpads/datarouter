package com.hotpads.datarouter.storage.key.base;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.BaseKey;

@SuppressWarnings("serial")
public abstract class BaseIntegerKey<D extends Databean>
extends BaseKey<D>{

	protected Integer key;
	
	public BaseIntegerKey(Class<D> databeanClass, Integer key) {
		super(databeanClass);
		this.key = key;
	}

	public Integer getKey() {
		return key;
	}

	
	
}
