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

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.job.DatarouterJobStopperJob;
import io.datarouter.job.monitoring.JobRetriggeringJob;
import io.datarouter.job.monitoring.LongRunningTaskFailureAlertJob;
import io.datarouter.job.vacuum.JobLockVacuumJob;
import io.datarouter.job.vacuum.LongRunningTaskVacuumJob;
import io.datarouter.job.vacuum.StopJobRequestVacuumJob;
import io.datarouter.job.vacuum.TaskTrackerPublishJob;
import io.datarouter.job.vacuum.TriggerLockVacuumJob;
import io.datarouter.job.vacuum.TriggerLockVacuumUnlockJob;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.time.ZoneIds;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterJobTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterJobTriggerGroup(DatarouterJobSettingRoot settings){
		super("DatarouterJob", Tag.DATAROUTER, ZoneIds.AMERICA_NEW_YORK);
		registerLocked(
				"53 0/2 * * * ?",
				settings.runLongRunningTaskVacuum,
				LongRunningTaskVacuumJob.class,
				true);
		registerLocked(
				"12 0/5 * * * ?",
				settings.runTaskTrackingPublishingJob,
				TaskTrackerPublishJob.class,
				true);
		registerLocked(
				"39 0/1 * * * ?",
				settings.runjobLockVacuum,
				JobLockVacuumJob.class,
				true);
		registerParallel(
				"4 * * * * ?",
				settings.runjobLockVacuumUnlockJob,
				TriggerLockVacuumUnlockJob.class);
		registerLocked(
				"24 3 * * * ?",
				settings.runTriggerLockVacuumJob,
				TriggerLockVacuumJob.class,
				true);
		registerLocked(
				"44 1/15 * * * ?",
				settings.runStopJobRequestVacuumJob,
				StopJobRequestVacuumJob.class,
				true);
		registerLocked(
				"0 0 13 * * ?",
				settings.runTaskFailureAlertJob,
				LongRunningTaskFailureAlertJob.class,
				true);
		registerParallel(
				"22 3/20 * * * ?",
				settings.runJobRetriggeringJob,
				JobRetriggeringJob.class);
		registerParallel(
				"7/10 * * * * ?",
				settings.runJobStopperJob,
				DatarouterJobStopperJob.class);
	}

}
