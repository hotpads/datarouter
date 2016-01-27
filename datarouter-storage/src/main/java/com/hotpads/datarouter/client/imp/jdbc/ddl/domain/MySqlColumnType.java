package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hotpads.datarouter.util.core.DrStringTool;

public enum MySqlColumnType{

	/*
	 * NOT TAKING INTO ACCOUNT OPTIONS AVAILABLE FOR THE DIFFERENT TYPES
	 */
	 //  Numeric Type Overview 
	BIT(true, false), 
	TINYINT(true, true), 
	BOOL(true, false), 
	BOOLEAN(true, true), 
	SMALLINT(true, false), 
	MEDIUMINT(false, false), 
	INT(true, false), 
	INTEGER(true, false), 
	BIGINT(true, false), 
	DECIMAL(true, false),
	DEC(true, false) /* SIMILAR TO DECIMAL, HAS 'FIXED' FOR COMPATIBILITY */, 
	FLOAT(false, false), 
	DOUBLE(false, false), 
	DOUBLE_PRECISION(false, false), /* FLOAT(P) */	
	// Date and Time Type Overview
	DATE(false, false), 
	DATETIME(true, false),
	TIMESTAMP(false, false), 
	TIME(false, false), 
	YEAR(false, false),
	
	// String Type Overview
	CHAR(true, false), 
	VARCHAR(true, false), 
	BINARY(true, false), 
	VARBINARY(true, false), 
	TINYBLOB(false, false), 
	TINYTEXT(false, false), 
	BLOB(false, false), //"Binary Long Array of Bytes"
	TEXT(false, false), 
	MEDIUMBLOB(false, false), 
	MEDIUMTEXT(false, false), 
	LONGBLOB(false, false), 
	LONGTEXT(false, false), 
	ENUM(true, false), 
	SET(true, false);
	
	private static Map<String,MySqlColumnType> OTHER_NAME_TO_TYPE = Maps.newHashMap();
	static{
		OTHER_NAME_TO_TYPE.put("INT UNSIGNED", BIGINT);
		OTHER_NAME_TO_TYPE.put("BIGINT UNSIGNED", BIGINT);
	}


	/**************************** static **********************************/
	
	public static final int 
		
		MAX_KEY_LENGTH = 767,//ERROR 1071 (42000): Specified key was too long; max key length is 767 bytes
		NUM_DECIMAL_SECONDS = 3, //setting the Fractional Seconds Precision to 3 for Datetime datatype
		LENGTH_50 = 50,
		MAX_LENGTH_VARCHAR = (1 << 8) - 1,
		MAX_LENGTH_VARBINARY = 767,
		MAX_LENGTH_LONGBLOB = (1 << 24) - 1,
		MAX_LENGTH_TEXT = (1 << 16) - 1,
		MAX_LENGTH_MEDIUMTEXT = (1 << 24) - 1,
		INT_LENGTH_LONGTEXT = Integer.MAX_VALUE;//use this to get schema-update to create a LONGTEXT field

	public static final long
		MAX_LENGTH_LONGTEXT = (1L << 32) - 1;
	
	
	/**************************** fields ***********************************/
	
	private boolean specifyLength;
	private boolean supportsDefaultValue;
	
	
	/*********************** constructors *************************************/
	
	private MySqlColumnType(boolean specifyLength, boolean supportsDefaultValue){		
		this.specifyLength=specifyLength;
		this.supportsDefaultValue = supportsDefaultValue;
	}
	
	
	/************************ static methods ******************************************/
	
	public static MySqlColumnType parse(String a){
		String upperCase = DrStringTool.nullSafe(a).toUpperCase();
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
		ArrayList<String> list = new ArrayList<>();
		for(MySqlColumnType type : values()){
			list.add(type.toString());
		}
		return list;
	}
	
	
	/*************************** get/set *******************************************/
	
	public boolean shouldSpecifyLength(Integer specifiedLength){	
		if(specifiedLength == null){
			return false;
		}
		if(this.equals(DATETIME) && specifiedLength == 0){			
			return false;
		}		
		return specifyLength;
	}
	
	public boolean isDefaultValueSupported(){
		return supportsDefaultValue;
	}
	
	
	/************************ main ***********************************************/

	public static void main(String[] args){
		System.out.println(LENGTH_50 + " " +MAX_LENGTH_VARCHAR + " " + MAX_LENGTH_TEXT + " " + " " + MAX_LENGTH_MEDIUMTEXT + " " + MAX_LENGTH_LONGTEXT);
	}
	
}
