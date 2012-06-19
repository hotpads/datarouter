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
	
	public static final Long 
			MAX_LENGTH_VARCHAR = (1L << 8) - 1,
			MAX_LENGTH_TEXT = (1L << 16) - 1,
			MAX_LENGTH_MEDIUMTEXT = (1L << 24) - 1,
			MAX_LENGTH_LONGTEXT = (1L << 32) - 1;

	public static void main(String[] args){
		System.out.println(MAX_LENGTH_VARCHAR + " " + MAX_LENGTH_TEXT + " " + " " + MAX_LENGTH_MEDIUMTEXT + " " + MAX_LENGTH_LONGTEXT);
	}
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
