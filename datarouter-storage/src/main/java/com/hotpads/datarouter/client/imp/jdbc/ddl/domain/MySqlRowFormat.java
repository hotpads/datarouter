package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import com.hotpads.datarouter.util.core.DrStringTool;

public enum MySqlRowFormat{
	Compact,
	Dynamic,
	Fixed,
	Compressed,
	Redundant;

 public static MySqlRowFormat parse(String stringValue){
	 String lowerCase = DrStringTool.toLowerCase(stringValue);
	 for(MySqlRowFormat rowFormat : values()){
		 if(rowFormat.toString().equals(lowerCase)){
			 return rowFormat;
		 }
	 }
	 return null;
 }

}
