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

import java.util.ArrayList;
import java.util.List;

import io.datarouter.autoconfig.service.AutoConfigListener;
import io.datarouter.autoconfig.service.AutoConfigListener.NoOpAutoConfigListener;
import io.datarouter.autoconfig.service.AutoConfigService;
import io.datarouter.autoconfig.service.AutoConfigServiceClasses;
import io.datarouter.web.config.BaseWebPlugin;

public class DatarouterAutoConfigPlugin extends BaseWebPlugin{

	private final Class<? extends AutoConfigListener> autoConfigListener;
	private final List<Class<? extends AutoConfigService>> autoConfigServiceClasses;

	private DatarouterAutoConfigPlugin(
			Class<? extends AutoConfigListener> autoConfigListener,
			List<Class<? extends AutoConfigService>> autoConfigServiceClasses){

		this.autoConfigListener = autoConfigListener;
		this.autoConfigServiceClasses = autoConfigServiceClasses;

		if(!autoConfigListener.isInstance(NoOpAutoConfigListener.class)){
			addAppListener(autoConfigListener);
		}
		addRouteSet(DatarouterAutoConfigRouteSet.class);
	}

	@Override
	public String getName(){
		return "DatarouterAutoConfig";
	}

	@Override
	public void configure(){
		bind(AutoConfigListener.class).to(autoConfigListener);
		bind(AutoConfigServiceClasses.class).toInstance(new AutoConfigServiceClasses(autoConfigServiceClasses));
	}

	public static class DatarouterAutoConfigPluginBuilder{

		private Class<? extends AutoConfigListener> autoConfigListener = NoOpAutoConfigListener.class;
		private List<Class<? extends AutoConfigService>> autoConfigServiceClasses = new ArrayList<>();

		public DatarouterAutoConfigPluginBuilder setAutoConfigListener(
				Class<? extends AutoConfigListener> autoConfigListener){
			this.autoConfigListener = autoConfigListener;
			return this;
		}

		public DatarouterAutoConfigPluginBuilder addAutoConfigService(
				Class<? extends AutoConfigService> autoConfigService){
			autoConfigServiceClasses.add(autoConfigService);
			return this;
		}

		public DatarouterAutoConfigPlugin build(){
			return new DatarouterAutoConfigPlugin(autoConfigListener, autoConfigServiceClasses);
		}

	}

}
