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
import io.datarouter.auth.web.job.DatarouterInactivityRoleResetJob;
import io.datarouter.auth.web.job.DatarouterPermissionRequestVacuumJob;
import io.datarouter.auth.web.job.DatarouterSessionVacuumJob;
import io.datarouter.auth.web.job.DeletedRoleCleanupJob;
import io.datarouter.auth.web.job.SamlAuthnRequestRedirectUrlVacuumJob;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.util.DatarouterCronTool;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.time.ZoneIds;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAuthTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterAuthTriggerGroup(
			ServiceName serviceNameSupplier,
			ServerName serverNameSupplier,
			DatarouterAuthSettingRoot settings){
		super("DatarouterAuth", Tag.DATAROUTER, ZoneIds.AMERICA_NEW_YORK);
		registerLocked(
				DatarouterCronTool.everyHour(serviceNameSupplier.get(), "SamlAuthnRequestRedirectUrlVacuumJob"),
				settings.runSamlAuthnRequestRedirectUrlVacuumJob,
				SamlAuthnRequestRedirectUrlVacuumJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyHour(serviceNameSupplier.get(), "DatarouterAccountCredentialCleanupJob"),
				() -> true,
				DatarouterAccountCredentialCleanupJob.class,
				true);
		registerParallel(
				DatarouterCronTool.everyNSeconds(5, serverNameSupplier.get(), "DatarouterAccountLastUsedFlushJob"),
				settings.runDatarouterAccountLastUsedFlushJob,
				DatarouterAccountLastUsedFlushJob.class);
		registerLocked(
				DatarouterCronTool.everyNMinutes(2, serviceNameSupplier.get(), "DatarouterSessionVacuumJob"),
				settings.runUserSessionVacuumJob,
				DatarouterSessionVacuumJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyDay(serviceNameSupplier.get(), "DatarouterPermissionRequestVacuumJob"),
				settings.runPermissionRequestVacuumJob,
				DatarouterPermissionRequestVacuumJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyDay(serviceNameSupplier.get(), "DeletedRoleCleanupJob"),
				settings.runDeletedRoleCleanupJob,
				DeletedRoleCleanupJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyDay(serviceNameSupplier.get(), "DatarouterInactivityRoleResetJob"),
				settings.runInactivityRoleResetJob,
				DatarouterInactivityRoleResetJob.class,
				true);

		int refreshFrequencySeconds = (int)settings.accountRefreshFrequencyDuration.get().toSecond();
		registerParallel(
				DatarouterCronTool.everyNSeconds(
						refreshFrequencySeconds,
						serverNameSupplier.get(),
						"AccountPermissionCacheRefreshJob"),
				() -> true,
				AccountPermissionCacheRefreshJob.class);
	}

}
