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
package io.datarouter.client.mysql.op;

import java.sql.Connection;

import io.datarouter.storage.config.ConfigKey;
import io.datarouter.storage.config.ConfigValue;
import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.util.enums.IntegerEnum;
import io.datarouter.util.enums.StringEnum;

public enum Isolation implements IntegerEnum<Isolation>, StringEnum<Isolation>, ConfigValue<Isolation>{

	serializable(Connection.TRANSACTION_SERIALIZABLE, 20, "serializable"),
	repeatableRead(Connection.TRANSACTION_REPEATABLE_READ, 21, "repeatableRead"),
	readCommitted(Connection.TRANSACTION_READ_COMMITTED, 22, "readCommitted"),
	readUncommitted(Connection.TRANSACTION_READ_UNCOMMITTED, 23, "readUncommitted");

	public static final ConfigKey<Isolation> KEY = new ConfigKey<>("isolation");
	public static final Isolation DEFAULT = readCommitted;

	private final Integer jdbcVal;
	private final int persistentInteger;
	private final String persistentString;

	Isolation(int jdbcVal, int persistentInteger, String persistentString){
		this.jdbcVal = jdbcVal;
		this.persistentInteger = persistentInteger;
		this.persistentString = persistentString;
	}

	public Integer getJdbcVal(){
		return jdbcVal;
	}

	@Override
	public Integer getPersistentInteger(){
		return persistentInteger;
	}

	@Override
	public Isolation fromPersistentInteger(Integer persistentInteger){
		return DatarouterEnumTool.getEnumFromInteger(values(), persistentInteger, null);
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public Isolation fromPersistentString(String persistentString){
		return DatarouterEnumTool.getEnumFromString(values(), persistentString, null);
	}

	@Override
	public ConfigKey<Isolation> getKey(){
		return KEY;
	}

}
