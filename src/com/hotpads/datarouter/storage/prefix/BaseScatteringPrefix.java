package com.hotpads.datarouter.storage.prefix;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class BaseScatteringPrefix<PK extends PrimaryKey<PK>>
implements ScatteringPrefix<PK>{

	protected PK key;
	
	@Override
	public void setKey(PK key){
		this.key = key;
	}

//	@Override
//	public List<Field<?>> getScatteringPrefixFields(PK primaryKey){
//		return ListTool.create();
//	}
//	
//	@Override
//	public List<List<Field<?>>> getAllPossibleScatteringPrefixes(){
//		List<List<Field<?>>> out = ListTool.create();
//		return out;
//	}
}
