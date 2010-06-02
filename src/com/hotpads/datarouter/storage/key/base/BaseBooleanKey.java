package com.hotpads.datarouter.storage.key.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseBooleanKey<PK extends PrimaryKey<PK>> 
extends BaseKey<PK>{

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
