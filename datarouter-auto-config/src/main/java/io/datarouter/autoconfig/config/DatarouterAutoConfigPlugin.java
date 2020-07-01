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
package io.datarouter.autoconfig.config;

import java.util.HashSet;
import java.util.Set;

import io.datarouter.autoconfig.service.AutoConfig;
import io.datarouter.autoconfig.service.AutoConfigGroup;
import io.datarouter.autoconfig.service.AutoConfigListener;
import io.datarouter.autoconfig.service.AutoConfigListener.NoOpAutoConfigListener;
import io.datarouter.autoconfig.service.AutoConfigRegistry;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterAutoConfigPlugin extends BaseWebPlugin{

	private static final DatarouterAutoConfigPaths PATHS = new DatarouterAutoConfigPaths();

	private final Class<? extends AutoConfigListener> autoConfigListener;
	private final Set<Class<? extends AutoConfig>> autoConfigs;
	private final Set<Class<? extends AutoConfigGroup>> autoConfigGroups;

	private DatarouterAutoConfigPlugin(
			Class<? extends AutoConfigListener> autoConfigListener,
			Set<Class<? extends AutoConfig>> autoConfigs,
			Set<Class<? extends AutoConfigGroup>> autoConfigGroups){
		this.autoConfigListener = autoConfigListener;
		this.autoConfigs = autoConfigs;
		this.autoConfigGroups = autoConfigGroups;

		if(!autoConfigListener.isInstance(NoOpAutoConfigListener.class)){
			addAppListener(autoConfigListener);
		}
		addRouteSet(DatarouterAutoConfigRouteSet.class);
		addDatarouterNavBarItem(DatarouterNavBarCategory.INFO, PATHS.datarouter.autoConfigs.viewAutoConfigs,
				"AutoConfigs");
	}

	@Override
	public String getName(){
		return "DatarouterAutoConfig";
	}

	@Override
	public void configure(){
		bind(AutoConfigListener.class).to(autoConfigListener);
		bind(AutoConfigRegistry.class).toInstance(new AutoConfigRegistry(autoConfigs, autoConfigGroups));
	}

	public static class DatarouterAutoConfigPluginBuilder{

		private Class<? extends AutoConfigListener> autoConfigListener = NoOpAutoConfigListener.class;
		private Set<Class<? extends AutoConfig>> autoConfigs = new HashSet<>();
		private Set<Class<? extends AutoConfigGroup>> autoConfigGroups = new HashSet<>();

		public DatarouterAutoConfigPluginBuilder setAutoConfigListener(
				Class<? extends AutoConfigListener> autoConfigListener){
			this.autoConfigListener = autoConfigListener;
			return this;
		}

		public DatarouterAutoConfigPluginBuilder addAutoConfig(Class<? extends AutoConfig> autoConfig){
			autoConfigs.add(autoConfig);
			return this;
		}

		public DatarouterAutoConfigPluginBuilder addAutoConfigGroup(Class<? extends AutoConfigGroup> autoConfigGroup){
			autoConfigGroups.add(autoConfigGroup);
			return this;
		}

		public DatarouterAutoConfigPlugin build(){
			return new DatarouterAutoConfigPlugin(autoConfigListener, autoConfigs, autoConfigGroups);
		}

	}

}
