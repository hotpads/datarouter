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

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.setting.impl.DatarouterClientAvailabilitySettings;
import io.datarouter.storage.config.setting.impl.DatarouterEmailSettings;
import io.datarouter.storage.config.setting.impl.DatarouterProfilingSettings;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterClusterSettings extends SettingRoot implements DatarouterSettings{

	private final CachedSetting<Boolean> recordCallsites;
	public final CachedSetting<Set<String>> additionalAdministratorsCsv;

	@Inject
	public DatarouterClusterSettings(SettingFinder finder, DatarouterProfilingSettings profilingSettings,
			DatarouterClientAvailabilitySettings clientAvailabilitySettings,
			DatarouterEmailSettings datarouterEmailSettings){
		super(finder, "datarouter.");
		registerChild(profilingSettings);
		registerChild(clientAvailabilitySettings);
		registerChild(datarouterEmailSettings);

		recordCallsites = registerBoolean("recordCallsites", false);
		additionalAdministratorsCsv = registerCommaSeparatedString("additionalAdministratorsCsv",
				Collections.emptySet());
	}

	@Override
	public Setting<Boolean> getRecordCallsites(){
		return recordCallsites;
	}

}
