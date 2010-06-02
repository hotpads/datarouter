package com.hotpads.datarouter.storage.key.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseLongKey<PK extends PrimaryKey<PK>>
extends BaseKey<PK>{

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
