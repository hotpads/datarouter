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
package io.datarouter.loggerconfig.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterLoggerConfigSettingRoot extends SettingRoot{

	public final Setting<Integer> loggingConfigMaxAgeDays;
	public final Setting<Integer> loggingConfigSendEmailAlertDays;
	public final CachedSetting<Boolean> runLoggerConfigUpdaterJob;
	public final CachedSetting<Boolean> runLoggerConfigCleanupJob;
	public final CachedSetting<Boolean> handleLoggerConfigDeletionAlerts;
	public final CachedSetting<Boolean> sendLoggerConfigUpdateAlerts;
	public final CachedSetting<Boolean> publishChangelog;

	@Inject
	public DatarouterLoggerConfigSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterLoggerConfig.");

		loggingConfigMaxAgeDays = registerInteger("loggingConfigMaxAgeDays", 70);
		loggingConfigSendEmailAlertDays = registerInteger("loggingConfigSendEmailAlertDays", 10);
		handleLoggerConfigDeletionAlerts = registerBoolean("handleLoggerConfigDeletionAlerts", false);
		runLoggerConfigUpdaterJob = registerBoolean("runLoggerConfigUpdaterJob", true);
		runLoggerConfigCleanupJob = registerBoolean("runLoggerConfigCleanupJob", false);
		sendLoggerConfigUpdateAlerts = registerBoolean("sendLoggerConfigUpdateAlerts", false);
		publishChangelog = registerBoolean("publishChangelog", false);
	}

}
