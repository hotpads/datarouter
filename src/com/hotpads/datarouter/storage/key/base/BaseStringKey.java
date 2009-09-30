package com.hotpads.datarouter.storage.key.base;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.BaseKey;

@SuppressWarnings("serial")
public abstract class BaseStringKey<D extends Databean> 
extends BaseKey<D>{

	protected String key;
	
	public BaseStringKey(Class<D> databeanClass, String key){
		super(databeanClass);
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	
	
}
