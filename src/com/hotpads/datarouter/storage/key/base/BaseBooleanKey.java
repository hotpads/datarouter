package com.hotpads.datarouter.storage.key.base;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.BaseKey;

@SuppressWarnings("serial")
public abstract class BaseBooleanKey<D extends Databean> 
extends BaseKey<D>{

	protected Boolean key;
	
	public BaseBooleanKey(Class<D> databeanClass, Boolean key){
		super(databeanClass);
		this.key = key;
	}

	public Boolean getKey() {
		return key;
	}

	
	
}
