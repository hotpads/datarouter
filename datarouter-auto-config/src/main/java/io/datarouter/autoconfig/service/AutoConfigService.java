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
package io.datarouter.autoconfig.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.plugin.PluginInjector;

@Singleton
public class AutoConfigService{

	private final Map<String,Callable<String>> autoConfigByName;

	@Inject
	public AutoConfigService(PluginInjector pluginInjector){
		autoConfigByName = new HashMap<>();
		pluginInjector.getInstances(AutoConfig.KEY)
				.forEach(autoConfig -> autoConfigByName.put(autoConfig.getName(), autoConfig));
		pluginInjector.getInstances(AutoConfigGroup.KEY)
				.forEach(autoConfigGroup -> autoConfigByName.put(autoConfigGroup.getName(), autoConfigGroup));
	}

	public Map<String,Callable<String>> getAutoConfigByName(){
		return autoConfigByName;
	}

}
