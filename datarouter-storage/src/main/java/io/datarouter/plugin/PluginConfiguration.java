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
package io.datarouter.plugin;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.scanner.Scanner;
import jakarta.inject.Singleton;

@Singleton
public class PluginConfiguration{

	private final Map<PluginConfigKey<?>,Class<? extends PluginConfigValue<?>>> classSingle;
	private final Map<PluginConfigKey<?>,List<Class<? extends PluginConfigValue<?>>>> classList;
	private final Map<PluginConfigKey<?>,PluginConfigValue<?>> instanceSingle;
	private final Map<PluginConfigKey<?>,List<PluginConfigValue<?>>> instanceList;

	public PluginConfiguration(
			Map<PluginConfigKey<?>,Class<? extends PluginConfigValue<?>>> classSingle,
			Map<PluginConfigKey<?>,List<Class<? extends PluginConfigValue<?>>>> classList,
			Map<PluginConfigKey<?>,PluginConfigValue<?>> instanceSingle,
			Map<PluginConfigKey<?>,List<PluginConfigValue<?>>> instanceList){
		this.classSingle = classSingle;
		this.classList = classList;
		this.instanceSingle = instanceSingle;
		this.instanceList = instanceList;
	}

	@SuppressWarnings("unchecked")
	public <T extends PluginConfigValue<T>> Optional<List<Class<T>>> findClassList(PluginConfigKey<T> key){
		List<Class<? extends PluginConfigValue<?>>> clazzes = classList.get(key);
		if(clazzes == null){
			return Optional.empty();
		}
		// intermediate step is needed for the compiler to figure out the types
		List<Class<T>> list = Scanner.of(clazzes)
				.map(clazz -> (Class<T>) clazz)
				.list();
		return Optional.ofNullable(list);
	}

	@SuppressWarnings("unchecked")
	public <T extends PluginConfigValue<T>> Optional<Class<T>> findClassSingle(PluginConfigKey<T> key){
		Class<T> clazz = (Class<T>) classSingle.get(key);
		return Optional.ofNullable(clazz);
	}

	@SuppressWarnings("unchecked")
	public <T extends PluginConfigValue<T>> Optional<List<T>> findInstanceList(PluginConfigKey<T> key){
		List<T> clazzes = (List<T>)instanceList.get(key);
		return Optional.ofNullable(clazzes);
	}

	@SuppressWarnings("unchecked")
	public <T extends PluginConfigValue<T>> Optional<T> findInstanceSingle(PluginConfigKey<T> key){
		return (Optional<T>) Optional.ofNullable(instanceSingle.get(key));
	}

}
