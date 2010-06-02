package com.hotpads.datarouter.storage.key.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseStringKey<PK extends PrimaryKey<PK>>
extends BaseKey<PK>{

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
