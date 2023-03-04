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
package io.datarouter.gcp.pubsub;

import java.util.List;

import io.datarouter.client.gcp.pubsub.config.PubSubPluginValidator;
import io.datarouter.client.gcp.pubsub.config.PubSubPluginValidator.DefaultPubSubPluginValidator;
import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.storage.config.properties.DatarouterTestPropertiesFile;
import io.datarouter.testng.TestNgModuleFactory;
import io.datarouter.web.config.DatarouterWebGuiceModule;

public class DatarouterPubsubTestNgModuleFactory extends TestNgModuleFactory{

	public DatarouterPubsubTestNgModuleFactory(){
		super(List.of(
				new DatarouterWebGuiceModule(),
				new PubsubTestGuiceModule()));
	}

	public static class PubsubTestGuiceModule extends BaseGuiceModule{

		@Override
		protected void configure(){
			bindActualInstance(DatarouterTestPropertiesFile.class,
					new DatarouterTestPropertiesFile("gcp-pubsub.properties"));
			bind(PubSubPluginValidator.class).to(DefaultPubSubPluginValidator.class);
		}

	}

}
