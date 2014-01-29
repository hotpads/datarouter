package com.hotpads.setting;

import com.hotpads.datarouter.storage.field.enums.StringEnum;

public interface ServerType<T> extends StringEnum<T>{

	public static final String
		ALL = "all",
		UNKNOWN = "unknown";
	
}
