package com.hotpads.datarouter.storage.key;

import com.hotpads.datarouter.storage.databean.Databean;

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
