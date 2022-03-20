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
package io.datarouter.storage.setting.cached.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.setting.DefaultSettingValue;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.cached.CachedSetting;

public class CommaSeparatedStringCachedSetting extends CachedSetting<Set<String>>{

	private static final String SEPARATOR = ",";

	public CommaSeparatedStringCachedSetting(
			SettingFinder finder,
			String name,
			DefaultSettingValue<Set<String>> defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	public boolean isValid(String value){
		return true;
	}

	@Override
	public Set<String> parseStringValue(String stringValue){
		if(stringValue.isEmpty()){
			return new HashSet<>();
		}
		return Scanner.of(stringValue.split(SEPARATOR))
				.collect(HashSet::new);
	}

	@Override
	public String toStringValue(Set<String> value){
		return value.stream()
			.sorted()
			.collect(Collectors.joining(SEPARATOR));
	}

}
