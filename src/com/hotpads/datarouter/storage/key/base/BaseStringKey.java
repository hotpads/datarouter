package com.hotpads.datarouter.storage.key.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.BaseKey;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseStringKey<D extends Databean> 
extends BaseKey<D>{

	@Column(nullable=false)
	protected String id;
	
	public BaseStringKey(Class<D> databeanClass, String key){
		super(databeanClass);
		this.id = key;
	}

	public String getId() {
		return id;
	}

	public void setId(String id){
		this.id = id;
	}

	
	
}
