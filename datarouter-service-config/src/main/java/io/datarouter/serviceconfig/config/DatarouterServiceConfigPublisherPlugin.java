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
package io.datarouter.serviceconfig.config;

import io.datarouter.instrumentation.serviceconfig.ServiceConfigurationPublisher;
import io.datarouter.instrumentation.serviceconfig.ServiceConfigurationPublisher.NoOpServiceConfigurationPublisher;
import io.datarouter.web.config.BaseWebPlugin;

public class DatarouterServiceConfigPublisherPlugin extends BaseWebPlugin{

	private final Class<? extends ServiceConfigurationPublisher> publisher;

	private DatarouterServiceConfigPublisherPlugin(Class<? extends ServiceConfigurationPublisher> publisher){
		this.publisher = publisher;
		addSettingRoot(DatarouterServiceConfigurationSettings.class);
		addAppListener(DatarouterServiceConfigurationAppListener.class);
		addDatarouterGithubDocLink("datarouter-service-config");
	}

	@Override
	public String getName(){
		return "DatarouterServiceConfig";
	}

	@Override
	public void configure(){
		bind(ServiceConfigurationPublisher.class).to(publisher);
	}

	public static class DatarouterServiceConfigPublisherPluginBuilder{

		private Class<? extends ServiceConfigurationPublisher> publisher = NoOpServiceConfigurationPublisher.class;

		public DatarouterServiceConfigPublisherPluginBuilder withServiceConfigurationPublisherClass(
				Class<? extends ServiceConfigurationPublisher> publisher){
			this.publisher = publisher;
			return this;
		}

		public DatarouterServiceConfigPublisherPlugin build(){
			return new DatarouterServiceConfigPublisherPlugin(publisher);
		}

	}

}
