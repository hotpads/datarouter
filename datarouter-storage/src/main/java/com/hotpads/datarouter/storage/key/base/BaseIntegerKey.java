package com.hotpads.datarouter.storage.key.base;


import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.datarouter.storage.key.Key;

@SuppressWarnings("serial")
public abstract class BaseIntegerKey<K extends Key<K>>
extends BaseKey<K>{

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
