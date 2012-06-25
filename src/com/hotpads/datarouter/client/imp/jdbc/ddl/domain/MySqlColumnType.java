package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import com.hotpads.util.core.StringTool;

public enum MySqlColumnType {

	/*
	 * NOT TAKING INTO ACCOUNT OPTIONS AVAILABLE FOR THE DIFFERENT TYPES
	 */
	 //  Numeric Type Overview 
	BIT(true), 
	TINYINT(true), 
	BOOL(true), 
	BOOLEAN(true), 
	SMALLINT(true), 
	MEDIUMINT(false), 
	INT(true), 
	INTEGER(true), 
	BIGINT(true), 
	DECIMAL(true),
	DEC(true) /* SIMILAR TO DECIMAL, HAS 'FIXED' FOR COMPATIBILITY */, 
	FLOAT(false), 
	DOUBLE(false), 
	DOUBLE_PRECISION(false), /* FLOAT(P) */
	
	// Date and Time Type Overview
	DATE(true), 
	DATETIME(true), 
	TIMESTAMP(true), 
	TIME(true), 
	YEAR(true),
	
	// String Type Overview
	CHAR(true), 
	VARCHAR(true), 
	BINARY(true), 
	VARBINARY(true), 
	TINYBLOB(false), 
	TINYTEXT(false), 
	BLOB(false), //"Binary Long Array of Bytes"
	TEXT(false), 
	MEDIUMBLOB(false), 
	MEDIUMTEXT(false), 
	LONGBLOB(false), 
	LONGTEXT(false), 
	ENUM(true), 
	SET(true);
	
	private boolean specifyLenght;
	
	private MySqlColumnType(boolean specifyLength){
		this.specifyLenght=specifyLength;
	}
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
	public boolean isSpecifyLenght(){
		return specifyLenght;
	}
	
}
