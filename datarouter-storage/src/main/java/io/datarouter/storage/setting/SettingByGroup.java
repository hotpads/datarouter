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
package io.datarouter.storage.setting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import io.datarouter.util.string.StringTool;

public class SettingByGroup<T>{

	private final String name;
	private final Map<String,Setting<T>> settings;
	private final BiFunction<String,T,Setting<T>> settingRegistrar;

	public SettingByGroup(String name, T defaultValue, BiFunction<String,T,Setting<T>> settingRegistrar){
		this.name = name;
		this.settings = new ConcurrentHashMap<>();
		this.settingRegistrar = settingRegistrar;
		addSetting("", defaultValue);
	}

	// we can avoid this method if we can register settings on the fly
	// that is, if the user creates a cluster setting using the UI
	public void addSetting(String group, T defaultValue){
		String settingName = getGroupSettingName(group);
		settings.put(settingName, settingRegistrar.apply(settingName, defaultValue));
	}

	public Setting<T> getSetting(String group){
		return settings.getOrDefault(getGroupSettingName(group), settings.get(name));
	}

	public Setting<T> getDefaultSetting(){
		return settings.get(name);
	}

	private String getGroupSettingName(String group){
		String groupSettingPostfix = StringTool.notEmpty(group) ? "." + group : "";
		return name + groupSettingPostfix;
	}

}
