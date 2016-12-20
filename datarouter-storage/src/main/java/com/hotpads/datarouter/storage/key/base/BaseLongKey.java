package com.hotpads.datarouter.storage.key.base;

import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.datarouter.storage.key.Key;

public abstract class BaseLongKey<K extends Key<K>>
extends BaseKey<K>{

	protected Long id;

	public BaseLongKey(Long id){
		this.id = id;
	}

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

}
