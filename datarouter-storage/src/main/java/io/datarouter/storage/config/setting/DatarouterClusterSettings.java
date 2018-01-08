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
package io.datarouter.storage.config.setting;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.setting.impl.BatchSizeOptimizerSettings;
import io.datarouter.storage.config.setting.impl.ClientAvailabilitySettings;
import io.datarouter.storage.config.setting.impl.DatarouterNotificationSettings;
import io.datarouter.storage.config.setting.impl.FailoverSettings;
import io.datarouter.storage.config.setting.impl.NodeWatchSettings;
import io.datarouter.storage.config.setting.impl.ProfilingSettings;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;

@Singleton
public class DatarouterClusterSettings extends SettingRoot implements DatarouterSettings{

	private final Setting<Boolean> loggingConfigUpdaterEnabled;
	private final Setting<Boolean> recordCallsites;
	private final Setting<Integer> numThreadsForMaxThreadsTest;


	@Inject
	public DatarouterClusterSettings(SettingFinder finder, DatarouterNotificationSettings notificationSettings,
			ProfilingSettings profilingSettings, BatchSizeOptimizerSettings batchSizeOptimizerSettings,
			NodeWatchSettings nodeWatchSettings, ClientAvailabilitySettings clientAvailabilitySettings,
			FailoverSettings failoverSettings){
		super(finder, "datarouter.");
		registerChild(notificationSettings);
		registerChild(profilingSettings);
		registerChild(batchSizeOptimizerSettings);
		registerChild(clientAvailabilitySettings);
		registerChild(nodeWatchSettings);
		registerChild(failoverSettings);

		loggingConfigUpdaterEnabled = registerBoolean("loggingConfigUpdaterEnabled", true);
		recordCallsites = registerBoolean("recordCallsites", false);
		numThreadsForMaxThreadsTest = registerInteger("numThreadsForMaxThreadsTest", 1);
	}


	@Override
	public Setting<Boolean> getLoggingConfigUpdaterEnabled(){
		return loggingConfigUpdaterEnabled;
	}

	@Override
	public Setting<Boolean> getRecordCallsites(){
		return recordCallsites;
	}

	@Override
	public Setting<Integer> getNumThreadsForMaxThreadsTest(){
		return numThreadsForMaxThreadsTest;
	}

}
