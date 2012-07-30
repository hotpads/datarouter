package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import com.hotpads.util.core.StringTool;

public enum MySqlColumnType{

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
	DATE(false), 
	DATETIME(false), 
	TIMESTAMP(false), 
	TIME(false), 
	YEAR(false),
	
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
	
	


	/**************************** static **********************************/
	
	public static final int 
		MAX_LENGTH_VARCHAR = (1 << 8) - 1,
		MAX_LENGTH_TEXT = (1 << 16) - 1,
		MAX_LENGTH_MEDIUMTEXT = (1 << 24) - 1;

	public static final long
		MAX_LENGTH_LONGTEXT = (1L << 32) - 1;
	
	
	/**************************** fields ***********************************/
	
	private boolean specifyLength;
	
	
	/*********************** constructors *************************************/
	
	private MySqlColumnType(boolean specifyLength){
		this.specifyLength=specifyLength;
	}
	
	
	/************************ static methods ******************************************/
	
	public static MySqlColumnType parse(String a){
		String upperCase = StringTool.nullSafe(a).toUpperCase();
		for(MySqlColumnType type : values()){
			if(type.toString().equals(upperCase)){
				return type;
			}
		}
		return null;
	}
	
	
	/*************************** get/set *******************************************/
	
	public boolean isSpecifyLength(){
		return specifyLength;
	}
	
	
	/************************ main ***********************************************/

	public static void main(String[] args){
		System.out.println(MAX_LENGTH_VARCHAR + " " + MAX_LENGTH_TEXT + " " + " " + MAX_LENGTH_MEDIUMTEXT + " " + MAX_LENGTH_LONGTEXT);
	}
	
}
