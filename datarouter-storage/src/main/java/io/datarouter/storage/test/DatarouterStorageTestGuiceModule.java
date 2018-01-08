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
package io.datarouter.storage.test;

import com.google.inject.AbstractModule;

import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.setting.DatarouterSettings;
import io.datarouter.storage.config.setting.NoOpDatarouterSettings;
import io.datarouter.storage.setting.MemorySettingFinder;
import io.datarouter.storage.setting.ServerType;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.StandardServerType;

public class DatarouterStorageTestGuiceModule extends AbstractModule{

	@Override
	protected void configure(){
		bind(DatarouterSettings.class).to(NoOpDatarouterSettings.class);
		bind(SettingFinder.class).to(MemorySettingFinder.class);
		bind(DatarouterProperties.class).to(TestDatarouterProperties.class);
		bind(ServerType.class).toInstance(StandardServerType.DEV);
	}

}
