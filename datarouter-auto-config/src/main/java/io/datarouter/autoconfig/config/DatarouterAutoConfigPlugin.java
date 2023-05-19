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
package io.datarouter.autoconfig.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.datarouter.autoconfig.service.AutoConfig;
import io.datarouter.autoconfig.service.AutoConfigGroup;
import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterAutoConfigPlugin extends BaseWebPlugin{

	private static final DatarouterAutoConfigPaths PATHS = new DatarouterAutoConfigPaths();

	private DatarouterAutoConfigPlugin(Map<PluginConfigKey<?>,List<Class<? extends PluginConfigValue<?>>>> configs){
		addRouteSet(DatarouterAutoConfigRouteSet.class);
		addRouteSet(DatarouterAutoConfigRawStringRouteSet.class);
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.CONFIGURATION,
				PATHS.datarouter.autoConfigs.viewAutoConfigs,
				"AutoConfigs");
		addDatarouterGithubDocLink("datarouter-auto-config");
		addPluginConfig(configs);
	}

	public static class DatarouterAutoConfigPluginBuilder{

		private Map<PluginConfigKey<?>,List<Class<? extends PluginConfigValue<?>>>> configs = new HashMap<>();

		// TODO Create a wrapper object of key and values - PluginConfigEntry?
		public DatarouterAutoConfigPluginBuilder addAutoConfig(Class<? extends PluginConfigValue<?>> config){
			configs.computeIfAbsent(AutoConfig.KEY, $ -> new ArrayList<>()).add(config);
			return this;
		}

		public DatarouterAutoConfigPluginBuilder addAutoConfigGroup(Class<? extends PluginConfigValue<?>> config){
			configs.computeIfAbsent(AutoConfigGroup.KEY, $ -> new ArrayList<>()).add(config);
			return this;
		}

		public DatarouterAutoConfigPlugin build(){
			return new DatarouterAutoConfigPlugin(configs);
		}

	}

}
