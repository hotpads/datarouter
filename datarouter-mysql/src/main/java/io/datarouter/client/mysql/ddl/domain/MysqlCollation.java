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
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.util.string.StringTool;

public enum MysqlCollation implements FielderConfigValue<MysqlCollation>{

	dec8_swedish_ci,
	dec8_bin,
	cp850_general_ci,
	cp850_bin,
	hp8_english_ci,
	hp8_bin,
	koi8r_general_ci,
	koi8r_bin,
	latin1_german1_ci,
	latin1_swedish_ci,
	latin1_danish_ci,
	latin1_german2_ci,
	latin1_bin,
	latin1_general_ci,
	latin1_general_cs,
	latin1_spanish_ci,
	latin2_general_ci,
	latin2_hungarian_ci,
	latin2_croatian_ci,
	latin2_bin,
	swe7_swedish_ci,
	swe7_bin,
	ascii_general_ci,
	ascii_bin,
	hebrew_general_ci,
	hebrew_bin,
	filename,
	koi8u_general_ci,
	koi8u_bin,
	greek_general_ci,
	greek_bin,
	cp1250_general_ci,
	cp1250_croatian_ci,
	cp1250_bin,
	cp1250_polish_ci,
	latin5_turkish_ci,
	latin5_bin,
	armscii8_general_ci,
	armscii8_bin,
	utf8_general_ci,
	utf8_bin,
	utf8_unicode_ci,
	utf8_icelandic_ci,
	utf8_latvian_ci,
	utf8_romanian_ci,
	utf8_slovenian_ci,
	utf8_polish_ci,
	utf8_estonian_ci,
	utf8_spanish_ci,
	utf8_swedish_ci,
	utf8_turkish_ci,
	utf8_czech_ci,
	utf8_danish_ci,
	utf8_lithuanian_ci,
	utf8_slovak_ci,
	utf8_spanish2_ci,
	utf8_roman_ci,
	utf8_persian_ci,
	utf8_esperanto_ci,
	utf8_hungarian_ci,
	utf8mb4_bin,
	utf8mb4_unicode_ci,
	cp866_general_ci,
	cp866_bin,
	keybcs2_general_ci,
	keybcs2_bin,
	macce_general_ci,
	macce_bin,
	macroman_general_ci,
	macroman_bin,
	cp852_general_ci,
	cp852_bin,
	latin7_estonian_cs,
	latin7_general_ci,
	latin7_general_cs,
	latin7_bin,
	cp1251_bulgarian_ci,
	cp1251_ukrainian_ci,
	cp1251_bin,
	cp1251_general_ci,
	cp1251_general_cs,
	cp1256_general_ci,
	cp1256_bin,
	cp1257_lithuanian_ci,
	cp1257_bin,
	cp1257_general_ci,
	binary,
	geostd8_general_ci,
	geostd8_bin;

	public static final FielderConfigKey<MysqlCollation> KEY = new FielderConfigKey<>("mySqlCollation");

	public static MysqlCollation parse(String stringValue){
		String lowerCase = StringTool.toLowerCase(stringValue);
		for(MysqlCollation collation : values()){
			if(collation.toString().equals(lowerCase)){
				return collation;
			}
		}
		return null;
	}

	public boolean isBinary(){
		return name().endsWith("bin");
	}

	@Override
	public FielderConfigKey<MysqlCollation> getKey(){
		return KEY;
	}

	public static boolean isCaseInsensitive(DatabeanFieldInfo<?,?,?> fieldInfo){
		return fieldInfo.getSampleFielder().getOption(KEY)
				.map(MysqlCollation::name)
				.map(str -> str.endsWith("ci"))
				.orElse(false); // assume default collation is not ci
	}

}
