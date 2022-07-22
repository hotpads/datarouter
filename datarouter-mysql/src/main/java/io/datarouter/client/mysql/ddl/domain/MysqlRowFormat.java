/*
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

import io.datarouter.enums.StringMappedEnum;
import io.datarouter.model.serialize.fielder.FielderConfigKey;
import io.datarouter.model.serialize.fielder.FielderConfigValue;

public enum MysqlRowFormat implements FielderConfigValue<MysqlRowFormat>{
	COMPACT("Compact"),
	DYNAMIC("Dynamic"),
	FIXED("Fixed"),
	COMPRESSED("Compressed"),
	REDUNDANT("Redundant");

	public static final StringMappedEnum<MysqlRowFormat> BY_VALUE
			= new StringMappedEnum<>(values(), value -> value.value);

	public static final FielderConfigKey<MysqlRowFormat> KEY = new FielderConfigKey<>("mySqlRowFormat");

	public final String value;

	MysqlRowFormat(String value){
		this.value = value;
	}

	@Override
	public FielderConfigKey<MysqlRowFormat> getKey(){
		return KEY;
	}

}
