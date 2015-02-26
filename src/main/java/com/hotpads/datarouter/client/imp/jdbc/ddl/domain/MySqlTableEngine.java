package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import com.hotpads.datarouter.util.core.DrStringTool;

public enum MySqlTableEngine{

	INNODB, 
	MYISAM;
	
	
	public static MySqlTableEngine parse(String a){
		String upperCase = DrStringTool.nullSafe(a).toUpperCase();
		for(MySqlTableEngine type : values()){
			if(type.toString().equals(upperCase)){
				return type;
			}
		}
		return null;
	}
}
