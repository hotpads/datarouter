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

import io.datarouter.auth.web.job.AccountPermissionCacheRefreshJob;
import io.datarouter.auth.web.job.DatarouterAccountCredentialCleanupJob;
import io.datarouter.auth.web.job.DatarouterAccountLastUsedFlushJob;
import io.datarouter.auth.web.job.DatarouterPermissionRequestVacuumJob;
import io.datarouter.auth.web.job.DatarouterSessionVacuumJob;
import io.datarouter.auth.web.job.DeletedRoleCleanupJob;
import io.datarouter.auth.web.job.SamlAuthnRequestRedirectUrlVacuumJob;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.time.ZoneIds;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAuthTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterAuthTriggerGroup(DatarouterAuthSettingRoot settings){
		super("DatarouterAuth", Tag.DATAROUTER, ZoneIds.AMERICA_NEW_YORK);
		registerLocked(
				"50 20 * * * ?",
				settings.runSamlAuthnRequestRedirectUrlVacuumJob,
				SamlAuthnRequestRedirectUrlVacuumJob.class,
				true);
		registerLocked(
				"45 47 * * * ?",
				() -> true,
				DatarouterAccountCredentialCleanupJob.class,
				true);
		registerParallel(
				"0/5 * * * * ?",
				settings.runDatarouterAccountLastUsedFlushJob,
				DatarouterAccountLastUsedFlushJob.class);
		registerLocked(
				"29 1/2 * * * ?",
				settings.runUserSessionVacuumJob,
				DatarouterSessionVacuumJob.class,
				true);
		registerLocked(
				"0 0 15 ? * * *",
				settings.runPermissionRequestVacuumJob,
				DatarouterPermissionRequestVacuumJob.class,
				true);
		registerLocked(
				"0 0 2 ? * *",
				settings.runDeletedRoleCleanupJob,
				DeletedRoleCleanupJob.class,
				true);

		long refreshFrequencySeconds = settings.accountRefreshFrequencyDuration.get()
				.toSecond();
		registerParallel(
				"1/" + refreshFrequencySeconds + " * * * * ?",
				() -> true,
				AccountPermissionCacheRefreshJob.class);
	}

}
