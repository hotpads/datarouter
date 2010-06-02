package com.hotpads.datarouter.storage.key.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseIntegerKey<PK extends PrimaryKey<PK>>
extends BaseKey<PK>{

	@Column(nullable=false)
	protected Integer id;
	
	public BaseIntegerKey(Integer id) {
		this.id = id;
	}

	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id = id;
	}

	
	
}
