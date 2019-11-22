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
package io.datarouter.storage.config.guice;

import java.util.Collections;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.inject.guice.GuiceInjector;
import io.datarouter.storage.client.ClientInitializationTracker;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.config.setting.DatarouterCallsiteSettings;
import io.datarouter.storage.config.setting.DatarouterStorageSettingRoot;
import io.datarouter.storage.metric.Metrics;
import io.datarouter.storage.metric.Metrics.NoOpMetris;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.setting.AdditionalSettingRoots;
import io.datarouter.storage.setting.AdditionalSettingRootsSupplier;
import io.datarouter.storage.setting.MemorySettingFinder;
import io.datarouter.storage.setting.SettingFinder;

public class DatarouterStorageGuiceModule extends BaseGuiceModule{

	@Override
	protected void configure(){
		bind(DatarouterInjector.class).to(GuiceInjector.class);
		bind(DatarouterCallsiteSettings.class).to(DatarouterStorageSettingRoot.class);

		bindDefault(SettingFinder.class, MemorySettingFinder.class);
		bindDefault(Metrics.class, NoOpMetris.class);
		bindDefaultInstance(AdditionalSettingRootsSupplier.class, new AdditionalSettingRoots(Collections.emptyList()));

		// Necessary explicit bindings when dealing with child injectors
		bind(GuiceInjector.class);
		bind(DatarouterNodes.class);
		bind(ClientOptions.class);
		bind(ClientInitializationTracker.class);
	}

}
