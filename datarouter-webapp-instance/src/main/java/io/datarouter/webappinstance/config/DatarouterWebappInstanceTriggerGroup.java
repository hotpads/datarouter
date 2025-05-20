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
package io.datarouter.webappinstance.config;

import java.time.Duration;
import java.time.LocalTime;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.util.DatarouterCronDayOfWeek;
import io.datarouter.job.util.DatarouterCronTool;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.time.ZoneIds;
import io.datarouter.webappinstance.job.DeadClusterJobLockVacuumJob;
import io.datarouter.webappinstance.job.WebappInstanceAlertJob;
import io.datarouter.webappinstance.job.WebappInstanceLogTruncationJob;
import io.datarouter.webappinstance.job.WebappInstanceUpdateJob;
import io.datarouter.webappinstance.job.WebappInstanceVacuumJob;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterWebappInstanceTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterWebappInstanceTriggerGroup(
			ServiceName serviceNameSupplier,
			ServerName serverNameSupplier,
			DatarouterWebappInstanceSettingRoot settings){
		super("DatarouterWebappInstance", Tag.DATAROUTER, ZoneIds.AMERICA_NEW_YORK);
		registerParallel(
				DatarouterCronTool.everyNSeconds(
						WebappInstanceUpdateJob.WEBAPP_INSTANCE_UPDATE_SECONDS_DELAY,
						serverNameSupplier.get(),
						"WebappInstanceUpdateJob"),
				() -> true,
				WebappInstanceUpdateJob.class);
		registerParallel(
				DatarouterCronTool.onDaysOfWeekAfter(
						DatarouterCronDayOfWeek.weekdays(),
						LocalTime.of(15, 0, 0),
						Duration.ofMinutes(30),
						serverNameSupplier.get(),
						"WebappInstanceAlertJob"),
				settings.alertOnStaleWebappInstance,
				WebappInstanceAlertJob.class);
		registerLocked(
				DatarouterCronTool.everyNMinutes(10, serviceNameSupplier.get(), "WebappInstanceVacuumJob"),
				settings.runWebappInstanceVacuumJob,
				WebappInstanceVacuumJob.class,
				true);
		registerParallel(
				DatarouterCronTool.everyNMinutes(20, serverNameSupplier.get(), "DeadClusterJobLockVacuumJob"),
				settings.runDeadClusterJobLockVacuumJob,
				DeadClusterJobLockVacuumJob.class);
		registerLocked(
				DatarouterCronTool.everyDay(serviceNameSupplier.get(), "DeadClusterJobLockVacuumJob"),
				settings.runWebappInstanceLogTruncationJob,
				WebappInstanceLogTruncationJob.class,
				true);
	}

}
