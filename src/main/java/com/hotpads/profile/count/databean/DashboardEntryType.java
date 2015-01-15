package com.hotpads.profile.count.databean;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum DashboardEntryType implements StringEnum<DashboardEntryType>{
	
	COUNTER("counter");

	private String value;
	
	private DashboardEntryType(String value){
		this.value = value;;
	}
	
	@Override
	public String getPersistentString(){
		return value;
	}

	@Override
	public DashboardEntryType fromPersistentString(String s){
		return DatarouterEnumTool.getEnumFromString(values(), s, null);
	}
}