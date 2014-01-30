package com.hotpads.datarouter.storage.key.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.datarouter.storage.key.Key;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseLongKey<K extends Key<K>>
extends BaseKey<K>{

	@Column(nullable=false)
	protected Long id;
	
	public BaseLongKey(Long id) {
		this.id = id;
	}

	
	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

	
}
