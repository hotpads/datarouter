package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import com.hotpads.datarouter.util.core.StringTool;

public enum MySqlTableEngine{

	INNODB, 
	MYISAM;
	
	
	public static MySqlTableEngine parse(String a){
		String upperCase = StringTool.nullSafe(a).toUpperCase();
		for(MySqlTableEngine type : values()){
			if(type.toString().equals(upperCase)){
				return type;
			}
		}
		return null;
	}
}
