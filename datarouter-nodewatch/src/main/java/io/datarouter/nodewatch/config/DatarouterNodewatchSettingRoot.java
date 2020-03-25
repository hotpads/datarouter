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
package io.datarouter.nodewatch.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterNodewatchSettingRoot extends SettingRoot{

	public final CachedSetting<Boolean> tableSamplerJob;
	public final CachedSetting<Boolean> tableCountJob;
	public final CachedSetting<Boolean> tableSizeMonitoringJob;
	public final CachedSetting<Boolean> runConfigurationScanReportEmailJob;
	public final CachedSetting<Boolean> enableOffsetting;

	@Inject
	public DatarouterNodewatchSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterNodewatch.");
		tableSamplerJob = registerBoolean("tableSamplerJob", false);
		tableCountJob = registerBoolean("tableCountJob", false);
		tableSizeMonitoringJob = registerBoolean("tableSizeMonitoringJob", false);
		runConfigurationScanReportEmailJob = registerBoolean("runConfigurationScanReportEmailJob", false);
		enableOffsetting = registerBoolean("enableOffsetting", true);
	}

}
