package com.hotpads.datarouter.storage.key.multi.base;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


public abstract class BaseStringLookup<PK extends PrimaryKey<PK>>
extends BaseSingleFieldLookup<PK,String>{

	public BaseStringLookup(String id){
		super(id);
	}

}
