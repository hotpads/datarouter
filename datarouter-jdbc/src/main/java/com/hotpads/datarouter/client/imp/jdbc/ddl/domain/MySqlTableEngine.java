package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import com.hotpads.datarouter.util.core.DrStringTool;

public enum MySqlTableEngine{

	INNODB,
	MYISAM;


	public static MySqlTableEngine parse(String str){
		String upperCase = DrStringTool.nullSafe(str).toUpperCase();
		for(MySqlTableEngine type : values()){
			if(type.toString().equals(upperCase)){
				return type;
			}
		}
		return null;
	}
}
