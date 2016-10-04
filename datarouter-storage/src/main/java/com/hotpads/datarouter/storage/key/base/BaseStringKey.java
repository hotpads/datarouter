package com.hotpads.datarouter.storage.key.base;


import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.datarouter.storage.key.Key;

public abstract class BaseStringKey<K extends Key<K>>
extends BaseKey<K>{

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
