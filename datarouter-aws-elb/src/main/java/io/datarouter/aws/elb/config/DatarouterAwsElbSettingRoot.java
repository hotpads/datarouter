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
package io.datarouter.aws.elb.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterAwsElbSettingRoot extends SettingRoot{

	public final CachedSetting<Boolean> runConfigurationScanReportEmailJob;

	@Inject
	public DatarouterAwsElbSettingRoot(SettingFinder finder, DatarouterAwsElbMonitoringSettings elbSettings){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterAwsElb.");

		registerChild(elbSettings);

		runConfigurationScanReportEmailJob = registerBoolean("runConfigurationScanReportEmailJob", false);
	}

}
