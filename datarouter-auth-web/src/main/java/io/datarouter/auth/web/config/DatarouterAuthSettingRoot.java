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
package io.datarouter.auth.web.config;

import java.util.concurrent.TimeUnit;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.duration.DatarouterDuration;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAuthSettingRoot extends SettingRoot{

	public final CachedSetting<Boolean> runSamlAuthnRequestRedirectUrlVacuumJob;
	public final CachedSetting<Boolean> runDatarouterAccountLastUsedFlushJob;
	public final CachedSetting<Boolean> runDeletedRoleCleanupJob;
	public final CachedSetting<Boolean> runUserSessionVacuumJob;
	public final CachedSetting<Boolean> runPermissionRequestVacuumJob;
	public final CachedSetting<Boolean> shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount;
	public final CachedSetting<Boolean> enableHandlerAccountCallerValidator;
	public final CachedSetting<DatarouterDuration> accountRefreshFrequencyDuration;
	public final CachedSetting<Boolean> enableAccountDailyDigest;

	@Inject
	public DatarouterAuthSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterAuth.");

		runSamlAuthnRequestRedirectUrlVacuumJob = registerBoolean("runSamlAuthnRequestRedirectUrlVacuumJob", false);
		runDatarouterAccountLastUsedFlushJob = registerBoolean("runDatarouterAccountLastUsedFlushJob", true);
		runDeletedRoleCleanupJob = registerBoolean("runDeletedRoleCleanupJob", true);
		runUserSessionVacuumJob = registerBoolean("runUserSessionVacuum", false);
		runPermissionRequestVacuumJob = registerBoolean("runPermissionRequestVacuumJob", false);
		shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount = registerBoolean(
				"shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount", true);
		enableHandlerAccountCallerValidator = registerBoolean("enableHandlerAccountCallerValidator", false);
		accountRefreshFrequencyDuration = registerDuration("accountRefreshFrequencyDuration",
				new DatarouterDuration(15, TimeUnit.SECONDS));
		enableAccountDailyDigest = registerBoolean("enableAccountDailyDigest", false);
	}

}
