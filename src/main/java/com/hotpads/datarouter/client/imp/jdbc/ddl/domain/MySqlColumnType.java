package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hotpads.util.core.ListTool;
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
	
	private static Map<String,MySqlColumnType> OTHER_NAME_TO_TYPE = Maps.newHashMap();
	static{
		OTHER_NAME_TO_TYPE.put("INT UNSIGNED", BIGINT);
		OTHER_NAME_TO_TYPE.put("BIGINT UNSIGNED", BIGINT);
	}


	/**************************** static **********************************/
	
	public static final int 
		LENGTH_50 = 50,
		MAX_LENGTH_VARCHAR = (1 << 8) - 1,
		MAX_LENGTH_TEXT = (1 << 16) - 1,
		MAX_LENGTH_MEDIUMTEXT = (1 << 24) - 1,
		INT_LENGTH_LONGTEXT = Integer.MAX_VALUE;//use this to get schema-update to create a LONGTEXT field

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
		MySqlColumnType type = OTHER_NAME_TO_TYPE.get(upperCase);
		if(type==null){
			throw new NullPointerException("Unparseable type: "+a);
		}
		return type;
	}
	
	public static List<String> getAllColumnTypeNames(){
		ArrayList<String> list = ListTool.createArrayList();
		for(MySqlColumnType type : values()){
			list.add(type.toString());
		}
		return list;
	}
	
	
	/*************************** get/set *******************************************/
	
	public boolean isSpecifyLength(){
		return specifyLength;
	}
	
	
	/************************ main ***********************************************/

	public static void main(String[] args){
		System.out.println(LENGTH_50 + " " +MAX_LENGTH_VARCHAR + " " + MAX_LENGTH_TEXT + " " + " " + MAX_LENGTH_MEDIUMTEXT + " " + MAX_LENGTH_LONGTEXT);
	}
	
}
