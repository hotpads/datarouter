package com.hotpads.datarouter.storage.key.multi.base;

import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class BaseSingleFieldLookup<PK extends PrimaryKey<PK>,T> extends BaseLookup<PK>{

	protected T id;

	public BaseSingleFieldLookup(T id){
		this.id = id;
	}

	public T getId(){
		return id;
	}

}
