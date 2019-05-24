/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.mysql.ddl.domain;

import java.util.HashMap;
import java.util.Map;

import io.datarouter.util.string.StringTool;

public enum MysqlColumnType{

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
	TIMESTAMP(true, false, false),
	TIME(false, false, false),
	YEAR(false, false, false),

	// String Type Overview
	CHAR(true, false, true),
	VARCHAR(true, true, true),
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

	private static Map<String,MysqlColumnType> OTHER_NAME_TO_TYPE = new HashMap<>();
	static{
		OTHER_NAME_TO_TYPE.put("INT UNSIGNED", BIGINT);
		OTHER_NAME_TO_TYPE.put("BIGINT UNSIGNED", BIGINT);
	}

	private final boolean specifyLength;
	private final boolean supportsDefaultValue;
	private final boolean isIntroducible;

	MysqlColumnType(boolean specifyLength, boolean supportsDefaultValue, boolean isIntroducible){
		this.specifyLength = specifyLength;
		this.supportsDefaultValue = supportsDefaultValue;
		this.isIntroducible = isIntroducible;
	}

	public static MysqlColumnType parse(String str){
		String upperCase = StringTool.nullSafe(str).toUpperCase();
		for(MysqlColumnType type : values()){
			if(type.toString().equals(upperCase)){
				return type;
			}
		}
		MysqlColumnType type = OTHER_NAME_TO_TYPE.get(upperCase);
		if(type == null){
			throw new NullPointerException("Unparseable type: " + str);
		}
		return type;
	}

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
