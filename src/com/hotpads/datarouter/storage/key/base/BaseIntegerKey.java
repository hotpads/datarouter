package com.hotpads.datarouter.storage.key.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.BaseKey;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseIntegerKey<D extends Databean>
extends BaseKey<D>{

	@Column(nullable=false)
	protected Integer id;
	
	public BaseIntegerKey(Class<D> databeanClass, Integer key) {
		super(databeanClass);
		this.id = key;
	}

	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id = id;
	}

	
	
}
