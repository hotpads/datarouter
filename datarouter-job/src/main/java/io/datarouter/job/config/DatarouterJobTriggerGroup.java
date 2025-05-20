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

import java.time.Duration;
import java.time.LocalTime;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.job.DatarouterJobStopperJob;
import io.datarouter.job.job.JobRetriggeringJob;
import io.datarouter.job.job.LongRunningTaskFailureAlertJob;
import io.datarouter.job.job.vacuum.JobLockVacuumJob;
import io.datarouter.job.job.vacuum.LongRunningTaskVacuumJob;
import io.datarouter.job.job.vacuum.StopJobRequestVacuumJob;
import io.datarouter.job.job.vacuum.TaskTrackerPublishJob;
import io.datarouter.job.job.vacuum.TriggerLockVacuumJob;
import io.datarouter.job.job.vacuum.TriggerLockVacuumUnlockJob;
import io.datarouter.job.util.DatarouterCronTool;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.time.ZoneIds;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterJobTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterJobTriggerGroup(
			ServiceName serviceNameSupplier,
			ServerName serverNameSupplier,
			DatarouterJobSettingRoot settings){
		super("DatarouterJob", Tag.DATAROUTER, ZoneIds.AMERICA_NEW_YORK);
		registerLocked(
				DatarouterCronTool.everyNMinutes(2, serviceNameSupplier.get(), "LongRunningTaskVacuumJob"),
				settings.runLongRunningTaskVacuum,
				LongRunningTaskVacuumJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyNMinutes(5, serviceNameSupplier.get(), "TaskTrackerPublishJob"),
				settings.runTaskTrackingPublishingJob,
				TaskTrackerPublishJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyMinute(serviceNameSupplier.get(), "JobLockVacuumJob"),
				settings.runjobLockVacuum,
				JobLockVacuumJob.class,
				true);
		registerParallel(
				DatarouterCronTool.everyMinute(serverNameSupplier.get(), "TriggerLockVacuumUnlockJob"),
				settings.runjobLockVacuumUnlockJob,
				TriggerLockVacuumUnlockJob.class);
		registerLocked(
				DatarouterCronTool.everyHour(serviceNameSupplier.get(), "TriggerLockVacuumJob"),
				settings.runTriggerLockVacuumJob,
				TriggerLockVacuumJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyNMinutes(15, serviceNameSupplier.get(), "StopJobRequestVacuumJob"),
				settings.runStopJobRequestVacuumJob,
				StopJobRequestVacuumJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyDayAfter(
						LocalTime.of(13, 0, 0),
						Duration.ofMinutes(5),
						serviceNameSupplier.get(),
						"LongRunningTaskFailureAlertJob"),
				settings.runTaskFailureAlertJob,
				LongRunningTaskFailureAlertJob.class,
				true);
		registerParallel(
				DatarouterCronTool.everyNMinutes(20, serverNameSupplier.get(), "JobRetriggeringJob"),
				settings.runJobRetriggeringJob,
				JobRetriggeringJob.class);
		registerParallel(
				DatarouterCronTool.everyNSeconds(10, serverNameSupplier.get(), "DatarouterJobStopperJob"),
				settings.runJobStopperJob,
				DatarouterJobStopperJob.class);
	}

}
