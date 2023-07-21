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
import java.util.Optional;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class PluginInjector{

	@Inject
	private PluginConfiguration pluginConfiguration;
	@Inject
	private DatarouterInjector datarouterInjector;

	public <T extends PluginConfigValue<T>> T getInstance(PluginConfigKey<T> key){
		if(key.type == PluginConfigType.INSTANCE_SINGLE){
			return pluginConfiguration.findInstanceSingle(key).orElse(null);
		}
		if(key.type == PluginConfigType.CLASS_SINGLE){
			Optional<Class<T>> clazz = pluginConfiguration.findClassSingle(key);
			if(clazz.isEmpty()){
				return null;
			}
			return datarouterInjector.getInstance(clazz.get());
		}
		return null;
	}

	public <T extends PluginConfigValue<T>> List<T> getInstances(PluginConfigKey<T> key){
		if(key.type == PluginConfigType.INSTANCE_LIST){
			Optional<List<T>> list = pluginConfiguration.findInstanceList(key);
			if(list.isEmpty()){
				return List.of();
			}
			return list.get();
		}
		if(key.type == PluginConfigType.CLASS_LIST){
			Optional<List<Class<T>>> list = pluginConfiguration.findClassList(key);
			if(list.isEmpty()){
				return List.of();
			}
			return Scanner.of(list.get())
					.map(datarouterInjector::getInstance)
					.list();
		}
		// TODO remove null
		return null;
	}

	public <T extends PluginConfigValue<T>> Scanner<T> scanInstances(PluginConfigKey<T> key){
		return Scanner.of(getInstances(key));
	}

}
