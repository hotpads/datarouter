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
package io.datarouter.loggerconfig.config;

import java.time.Duration;
import java.time.LocalTime;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.util.DatarouterCronDayOfWeek;
import io.datarouter.job.util.DatarouterCronTool;
import io.datarouter.loggerconfig.job.LoggerConfigCleanupJob;
import io.datarouter.loggerconfig.job.LoggerConfigUpdaterJob;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.time.ZoneIds;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterLoggerConfigTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterLoggerConfigTriggerGroup(
			ServiceName serviceNameSupplier,
			ServerName serverNameSupplier,
			DatarouterLoggerConfigSettingRoot settings){
		super("DatarouterLoggerConfig", Tag.DATAROUTER, ZoneIds.AMERICA_NEW_YORK);
		registerParallel(
				DatarouterCronTool.everyNSeconds(15, serverNameSupplier.get(), "LoggerConfigUpdaterJob"),
				settings.runLoggerConfigUpdaterJob,
				LoggerConfigUpdaterJob.class);
		registerLocked(
				DatarouterCronTool.onDaysOfWeekAfter(
						DatarouterCronDayOfWeek.weekdays(),
						LocalTime.of(15, 0, 0),
						Duration.ofMinutes(30),
						serviceNameSupplier.get(),
						"LoggerConfigCleanupJob"),
				settings.runLoggerConfigCleanupJob,
				LoggerConfigCleanupJob.class,
				true);
	}

}
