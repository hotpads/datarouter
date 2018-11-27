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

import io.datarouter.model.serialize.fielder.FielderConfigKey;
import io.datarouter.model.serialize.fielder.FielderConfigValue;
import io.datarouter.util.string.StringTool;

public enum MysqlCharacterSet implements FielderConfigValue<MysqlCharacterSet>{

	armscii8,
	ascii,
	binary,
	cp1250,
	cp1251,
	cp1256,
	cp1257,
	cp850,
	cp852,
	cp866,
	dec8,
	filename,
	geostd8,
	greek,
	hebrew,
	hp8,
	keybcs2,
	koi8r,
	koi8u,
	latin1,
	latin2,
	latin5,
	latin7,
	macce,
	macroman,
	swe7,
	utf8,
	utf8mb4;

	public static final FielderConfigKey<MysqlCharacterSet> KEY = new FielderConfigKey<>("mySqlCharacterSet");

	public static MysqlCharacterSet parse(String stringValue){
		String lowerCase = StringTool.toLowerCase(stringValue);
		for(MysqlCharacterSet charset : values()){
			if(charset.toString().equals(lowerCase)){
				return charset;
			}
		}
		return null;
	}

	@Override
	public FielderConfigKey<MysqlCharacterSet> getKey(){
		return KEY;
	}

}