package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import com.hotpads.util.core.enums.DatarouterEnumTool;
import com.hotpads.util.core.enums.StringEnum;

public enum MySqlRowFormat implements StringEnum<MySqlRowFormat>{
	COMPACT("Compact"),
	DYNAMIC("Dynamic"),
	FIXED("Fixed"),
	COMPRESSED("Compressed"),
	REDUNDANT("Redundant");

	private String value;

	private MySqlRowFormat(String value){
		this.value = value;
	}

	@Override
	public String getPersistentString(){
		return value;
	}

	@Override
	public MySqlRowFormat fromPersistentString(String string){
		return fromPersistentStringStatic(string);
	}

	public static MySqlRowFormat fromPersistentStringStatic(String str){
		return DatarouterEnumTool.getEnumFromString(values(), str, null);
	}


}

