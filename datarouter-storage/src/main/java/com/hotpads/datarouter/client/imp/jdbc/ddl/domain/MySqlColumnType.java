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
	BIT(true, false, false),
	TINYINT(true, true, false),
	BOOL(true, false, false),
	BOOLEAN(true, true, false),
	SMALLINT(true, false, false),
	MEDIUMINT(false, false, false),
	INT(true, false, false),
	INTEGER(true, false, false),
	BIGINT(true, false, false),
	DECIMAL(true, false, false),
	DEC(true, false, false) /* SIMILAR TO DECIMAL, HAS 'FIXED' FOR COMPATIBILITY */,
	FLOAT(false, false, false),
	DOUBLE(false, false, false),
	DOUBLE_PRECISION(false, false, false), /* FLOAT(P) */
	// Date and Time Type Overview
	DATE(false, false, false),
	DATETIME(true, false, false),
	TIMESTAMP(false, false, false),
	TIME(false, false, false),
	YEAR(false, false, false),

	// String Type Overview
	CHAR(true, false, true),
	VARCHAR(true, false, true),
	BINARY(true, false, false),
	VARBINARY(true, false, false),
	TINYBLOB(false, false, false),
	TINYTEXT(false, false, true),
	BLOB(false, false, false), //"Binary Long Array of Bytes"
	TEXT(false, false, true),
	MEDIUMBLOB(false, false, false),
	MEDIUMTEXT(false, false, true),
	LONGBLOB(false, false, false),
	LONGTEXT(false, false, true),
	ENUM(true, false, true),
	SET(true, false, true),
	GEOMETRY(false, false, false);

	private static Map<String,MySqlColumnType> OTHER_NAME_TO_TYPE = Maps.newHashMap();
	static{
		OTHER_NAME_TO_TYPE.put("INT UNSIGNED", BIGINT);
		OTHER_NAME_TO_TYPE.put("BIGINT UNSIGNED", BIGINT);
	}


	/**************************** static **********************************/

	public static final int

		MAX_KEY_LENGTH = 767,//ERROR 1071 (42000): Specified key was too long; max key length is 767 bytes
		MAX_KEY_LENGTH_UTF8MB4 = 191, // 767 / 4
		NUM_DECIMAL_SECONDS = 3, //setting the Fractional Seconds Precision to 3 for Datetime datatype
		LENGTH_50 = 50,
		DEFAULT_LENGTH_VARCHAR = (1 << 8) - 1,
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
	private boolean isIntroducible;

	/*********************** constructors *************************************/

	private MySqlColumnType(boolean specifyLength, boolean supportsDefaultValue, boolean isIntroducible){
		this.specifyLength = specifyLength;
		this.supportsDefaultValue = supportsDefaultValue;
		this.isIntroducible = isIntroducible;
	}


	/************************ static methods ******************************************/

	public static MySqlColumnType parse(String str){
		String upperCase = DrStringTool.nullSafe(str).toUpperCase();
		for(MySqlColumnType type : values()){
			if(type.toString().equals(upperCase)){
				return type;
			}
		}
		MySqlColumnType type = OTHER_NAME_TO_TYPE.get(upperCase);
		if(type == null){
			throw new NullPointerException("Unparseable type: " + str);
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

	/**
	 * This states whether the type can handle introducers in literals (in SQL queries).
	 * Only non-binary text types support this, because it affects the character set and collation that literals are
	 * interpreted as.
	 * Example: "_utf8 'hello' COLLATE utf8_bin" is an introduced form of "'hello'", which tells the DB that no
	 * conversion from the connection settings' character set/collation (defaulted to utf8mb4/utf8mb_bin) is necessary.
	 */
	public boolean isIntroducible(){
		return isIntroducible;
	}

}
