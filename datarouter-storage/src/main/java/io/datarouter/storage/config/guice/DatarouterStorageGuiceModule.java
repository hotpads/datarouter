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
package io.datarouter.storage.config.guice;

import java.util.Collections;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.inject.guice.GuiceInjector;
import io.datarouter.storage.client.ClientInitializationTracker;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.client.ClientOptionsFactory;
import io.datarouter.storage.client.ClientOptionsFactory.NoOpClientOptionsFactory;
import io.datarouter.storage.config.properties.DatarouterTestPropertiesFile;
import io.datarouter.storage.config.schema.SchemaUpdateOptionsFactory;
import io.datarouter.storage.config.schema.SchemaUpdateOptionsFactory.NoOpSchemaUpdateOptionsFactory;
import io.datarouter.storage.metric.Gauges;
import io.datarouter.storage.metric.Gauges.NoOpGauges;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypeDetector.NoOpServerTypeDetector;
import io.datarouter.storage.setting.MemorySettingFinder;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRootsSupplier;

public class DatarouterStorageGuiceModule extends BaseGuiceModule{

	@Override
	protected void configure(){
		bind(DatarouterInjector.class).to(GuiceInjector.class);

		bindDefault(SettingFinder.class, MemorySettingFinder.class);
		bindDefault(Gauges.class, NoOpGauges.class);
		bindDefaultInstance(SettingRootsSupplier.class, new SettingRootsSupplier(Collections.emptyList()));

		bindDefault(ClientOptionsFactory.class, NoOpClientOptionsFactory.class);
		bindDefault(SchemaUpdateOptionsFactory.class, NoOpSchemaUpdateOptionsFactory.class);

		// Necessary explicit bindings when dealing with child injectors
		bind(GuiceInjector.class);
		bind(DatarouterNodes.class);
		bind(ClientOptions.class);
		bind(ClientInitializationTracker.class);

		bindDefaultInstance(DatarouterTestPropertiesFile.class, new DatarouterTestPropertiesFile(""));

		bindDefault(ServerTypeDetector.class, NoOpServerTypeDetector.class);
	}

}
