package com.hotpads.job.record;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum LongRunningTaskType implements StringEnum<LongRunningTaskType>{

	job("job"),
	request("request"),
	migration("migration"),
	test("test");

	private final String varName;

	private LongRunningTaskType(String varName){
		this.varName = varName;
	}

	@Override
	public String getPersistentString() {
		return varName;
	}

	public static LongRunningTaskType fromPersistentStringStatic(String str){
		return DatarouterEnumTool.getEnumFromString(values(), str, null);
	}

	@Override
	public LongRunningTaskType fromPersistentString(String str) {
		return fromPersistentStringStatic(str);
	}
}
