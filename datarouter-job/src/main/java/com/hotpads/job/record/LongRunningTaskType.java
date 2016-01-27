package com.hotpads.job.record;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum LongRunningTaskType implements StringEnum<LongRunningTaskType>{

	job("job", "job"),
	request("request", "request"),
	migration("migration", "migration"),
	test("test", "test");
	
	private String display;
	private String varName;

	private LongRunningTaskType(String display, String varName){
		this.display = display;
		this.varName = varName;
	}
	
	@Override
	public String getPersistentString() {
		return varName;
	}

	public static LongRunningTaskType fromPersistentStringStatic(String s){
		return DatarouterEnumTool.getEnumFromString(values(), s, null);
	}
	
	@Override
	public LongRunningTaskType fromPersistentString(String s) {
		return fromPersistentStringStatic(s);
	}
}
