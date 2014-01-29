package com.hotpads.datarouter.storage.key.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.datarouter.storage.key.Key;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseStringKey<K extends Key<K>>
extends BaseKey<K>{

	@Column(nullable=false)
	protected String id;
	
	public BaseStringKey(String id){
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id){
		this.id = id;
	}

	
	
}
