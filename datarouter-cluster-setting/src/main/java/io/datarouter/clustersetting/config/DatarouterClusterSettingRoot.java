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
package io.datarouter.clustersetting.config;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.string.StringTool;

@Singleton
public class DatarouterClusterSettingRoot extends SettingRoot{

	public static final Set<String> EXCLUDED_SETTING_STRINGS = Set.of(
			"key",
			"password",
			"username",
			"secret",
			"token");

	public final CachedSetting<Integer> oldSettingAlertThresholdDays;
	public final CachedSetting<Set<String>> settingsExcludedFromOldSettingsAlert;
	public final CachedSetting<Set<String>> settingsExcludedFromUnknownSettingsAlert;
	public final CachedSetting<Boolean> sendUpdateEmail;

	public final CachedSetting<Boolean> runAlertJob;
	public final CachedSetting<Set<String>> alertJobRecipients;

	@Inject
	public DatarouterClusterSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterClusterSetting.");

		oldSettingAlertThresholdDays = registerInteger("oldSettingAlertThresholdDays", 14);
		settingsExcludedFromOldSettingsAlert = registerCommaSeparatedString("settingsExcludedFromOldSettingsAlert",
				EXCLUDED_SETTING_STRINGS);
		settingsExcludedFromUnknownSettingsAlert = registerCommaSeparatedString(
				"settingsExcludedFromUnknownSettingsAlert", Set.of());
		sendUpdateEmail = registerBoolean("sendUpdateEmail", false);

		runAlertJob = registerBoolean("runAlertJob", false);
		alertJobRecipients = registerCommaSeparatedString("alertJobRecipients", Set.of());
	}

	public boolean isExcludedOldSettingString(String settingName){
		return settingsExcludedFromOldSettingsAlert.get().stream()
				.anyMatch(setting -> StringTool.containsCaseInsensitive(settingName, setting));
	}

}
