package com.hotpads.datarouter.storage.key.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.BaseKey;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseBooleanKey<D extends Databean> 
extends BaseKey<D>{

	@Column(nullable=false)
	protected Boolean id;
	
	public BaseBooleanKey(Class<D> databeanClass, Boolean key){
		super(databeanClass);
		this.id = key;
	}

	public Boolean getId(){
		return id;
	}

	public void setId(Boolean id){
		this.id = id;
	}

	
	
}
