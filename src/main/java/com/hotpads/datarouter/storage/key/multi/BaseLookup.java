package com.hotpads.datarouter.storage.key.multi;

import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


@SuppressWarnings("serial")
public abstract class BaseLookup<PK extends PrimaryKey<PK>>
extends BaseKey<PK>
implements Lookup<PK>{

	protected PK key;
	
	public BaseLookup() {
	}
	
	public BaseLookup(PK primaryKey){
		this.key = primaryKey;
	}
	
	public PK getPrimaryKey(){
		return key;
	}
	
	@Override
	public void setPrimaryKey(PK primaryKey){
		this.key = primaryKey;
	}

}

