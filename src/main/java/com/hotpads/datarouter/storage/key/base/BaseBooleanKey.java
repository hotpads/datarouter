package com.hotpads.datarouter.storage.key.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.datarouter.storage.key.Key;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseBooleanKey<K extends Key<K>>
extends BaseKey<K>{

	@Column(nullable=false)
	protected Boolean id;
	
	public BaseBooleanKey(Boolean id){
		this.id = id;
	}

	public Boolean getId(){
		return id;
	}

	public void setId(Boolean id){
		this.id = id;
	}

	
	
}
