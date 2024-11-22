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
package io.datarouter.job.config;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterJobSettingRoot extends SettingRoot{

	public final CachedSetting<Boolean> runjobLockVacuum;
	public final CachedSetting<Boolean> runjobLockVacuumUnlockJob;
	public final CachedSetting<Boolean> runTriggerLockVacuumJob;
	public final CachedSetting<Boolean> runStopJobRequestVacuumJob;

	public final CachedSetting<Boolean> runLongRunningTaskVacuum;
	public final CachedSetting<Boolean> runTaskFailureAlertJob;

	public final CachedSetting<Boolean> processJobs;
	public final CachedSetting<Boolean> enableDetachedJobs;
	public final CachedSetting<Boolean> runJobRetriggeringJob;
	public final CachedSetting<Boolean> runJobStopperJob;
	public final CachedSetting<Boolean> runTaskTrackingPublishingJob;
	public final CachedSetting<Integer> taskTrackerPublisherPutMultiBatchSize;

	@Inject
	public DatarouterJobSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterJob.");

		runjobLockVacuum = registerBoolean("runClusterJobLockVacuum", true);
		runjobLockVacuumUnlockJob = registerBoolean("runClusterJobLockVacuumUnlockJob", true);
		runTriggerLockVacuumJob = registerBoolean("runClusterTriggerLockVacuumJob", true);
		runStopJobRequestVacuumJob = registerBoolean("runStopJobRequestVacuumJob", true);

		runLongRunningTaskVacuum = registerBoolean("runLongRunningTaskVacuum", false);
		// this is included with the daily-digest-summary email
		runTaskFailureAlertJob = registerBoolean("runTaskFailureAlertJob", false);

		processJobs = registerBoolean("processJobs", true);
		enableDetachedJobs = registerBoolean("enableDetachedJobs", true);
		runJobRetriggeringJob = registerBoolean("runJobRetriggeringJob", false);
		runJobStopperJob = registerBoolean("runJobStopperJob", true);

		runTaskTrackingPublishingJob = registerBoolean("runTaskTrackingPublishingJob", false);
		taskTrackerPublisherPutMultiBatchSize = registerInteger("taskTrackerPublisherPutMultiBatchSize", 50);
	}

}
