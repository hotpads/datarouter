package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum MySqlRowFormat implements StringEnum<MySqlRowFormat>{
	COMPACT("compact"),
	DYNAMIC("dynamic"),
	FIXED("fixed"),
	COMPRESSED("compressed"),
	REDUNDANT("redundant");

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
		return DatarouterEnumTool.getEnumFromString(values(), string, null);
	}

}

