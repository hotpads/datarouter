package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import com.hotpads.util.core.StringTool;

public enum MySqlColumnType {

	/*
	 * NOT TAKING INTO ACCOUNT OPTIONS AVAILABLE FOR THE DIFFERENT TYPES
	 */
	 //  Numeric Type Overview 
	BIT, 
	TINYINT, 
	BOOL, 
	BOOLEAN, 
	SMALLINT, 
	MEDIUMINT, 
	INT, 
	INTEGER, 
	BIGINT, 
	DECIMAL,
	DEC /* SIMILAR TO DECIMAL, HAS 'FIXED' FOR COMPATIBILITY */, 
	FLOAT, 
	DOUBLE, 
	DOUBLE_PRECISION, /* FLOAT(P) */
	
	// Date and Time Type Overview
	DATE, 
	DATETIME, 
	TIMESTAMP, 
	TIME, 
	YEAR,
	
	// String Type Overview
	CHAR, 
	VARCHAR, 
	BINARY, 
	VARBINARY, 
	TINYBLOB, 
	TINYTEXT, 
	BLOB, //"Binary Long Array of Bytes"
	TEXT, 
	MEDIUMBLOB, 
	MEDIUMTEXT, 
	LONGBLOB, 
	LONGTEXT, 
	ENUM, 
	SET;

	public static MySqlColumnType parse(String a){
		String upperCase = StringTool.nullSafe(a).toUpperCase();
		for(MySqlColumnType type : values()) {
			if(type.toString().equals(upperCase)) {
				return type;
			}
		}
		return null;
	}
}
