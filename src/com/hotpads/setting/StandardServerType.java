package com.hotpads.setting;

import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;

public enum StandardServerType implements ServerType<StandardServerType>{
	
	UNKNOWN("unknown"),
	ALL("all"),
	WEB("web"),
	JOBLET("joblet");
	
	private String persistentString;
	
	private StandardServerType(String persistentString){
		this.persistentString = persistentString;
	}

	@Override
	public String getPersistentString() {
		return persistentString;
	}
	
	public static StandardServerType fromPersistentStringStatic(String s){
		return DataRouterEnumTool.getEnumFromString(values(), s, null);
	}
	
	@Override
	public StandardServerType fromPersistentString(String s) {
		return fromPersistentStringStatic(s);
	}
	
	
}
