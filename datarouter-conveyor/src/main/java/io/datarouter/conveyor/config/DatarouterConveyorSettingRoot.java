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
package io.datarouter.conveyor.config;

import java.util.concurrent.TimeUnit;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.duration.DatarouterDuration;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterConveyorSettingRoot extends SettingRoot{

	public static String SETTING_NAME_PREFIX = "datarouterConveyor.";

	public final CachedSetting<DatarouterDuration> sleepOnTaskCompletion;
	public final CachedSetting<DatarouterDuration> pollTimeout;

	@Inject
	public DatarouterConveyorSettingRoot(
			SettingFinder finder,
			DatarouterConveyorShouldRunSettings conveyorShouldRunSettings,
			DatarouterConveyorThreadCountSettings conveyorThreadCountSettings,
			DatarouterConveyorTraceSettings conveyorTraceSettings){
		super(finder, DatarouterSettingCategory.DATAROUTER, SETTING_NAME_PREFIX);
		registerChild(conveyorShouldRunSettings);
		registerChild(conveyorThreadCountSettings);
		registerChild(conveyorTraceSettings);

		sleepOnTaskCompletion = registerDuration("sleepOnTaskCompletion", new DatarouterDuration(1, TimeUnit.SECONDS));
		pollTimeout = registerDuration("pollTimeout", new DatarouterDuration(10, TimeUnit.SECONDS));
	}

}
