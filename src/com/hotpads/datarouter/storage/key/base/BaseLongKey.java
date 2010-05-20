package com.hotpads.datarouter.storage.key.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.BaseKey;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseLongKey<D extends Databean>
extends BaseKey<D>{

	@Column(nullable=false)
	protected Long id;
	
	public BaseLongKey(Class<D> databeanClass, Long key) {
		super(databeanClass);
		this.id = key;
	}

	
	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

	
}
