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
package io.datarouter.nodewatch.config;

import java.time.Duration;
import java.time.LocalTime;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.util.DatarouterCronDayOfWeek;
import io.datarouter.job.util.DatarouterCronTool;
import io.datarouter.nodewatch.config.setting.DatarouterNodewatchSettingRoot;
import io.datarouter.nodewatch.job.NodewatchCostMonitoringJob;
import io.datarouter.nodewatch.job.TableCountJob;
import io.datarouter.nodewatch.job.TableSampleValidationJob;
import io.datarouter.nodewatch.job.TableSamplerJob;
import io.datarouter.nodewatch.job.TableSamplerJobletVacuumJob;
import io.datarouter.nodewatch.job.TableSizeMonitoringJob;
import io.datarouter.nodewatch.job.TableStorageSummaryJob;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.time.ZoneIds;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterNodewatchTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterNodewatchTriggerGroup(
			ServiceName serviceNameSupplier,
			DatarouterNodewatchSettingRoot settings){
		super("DatarouterNodewatch", Tag.DATAROUTER, ZoneIds.AMERICA_NEW_YORK);
		registerLocked(
				DatarouterCronTool.everyNMinutes(
						(int)TableSamplerJob.SCHEDULING_INTERVAL.toMinutes(),
						serviceNameSupplier.get(),
						"TableSamplerJob"),
				settings.tableSamplerJob,
				TableSamplerJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyNHours(4, serviceNameSupplier.get(), "TableCountJob"),
				settings.tableCountJob,
				TableCountJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyHour(serviceNameSupplier.get(), "TableSampleValidationJob"),
				settings.tableSampleValidationJob,
				TableSampleValidationJob.class,
				true);
		registerLocked(
				DatarouterCronTool.onDaysOfWeekAfter(
						DatarouterCronDayOfWeek.weekdays(),
						LocalTime.of(14, 0, 0),
						Duration.ofMinutes(30),
						serviceNameSupplier.get(),
						"TableSizeMonitoringJob"),
				settings.tableSizeMonitoringJob,
				TableSizeMonitoringJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyDay(serviceNameSupplier.get(), "TableSamplerJobletVacuumJob"),
				settings.runTableSamplerJobletVacuumJob,
				TableSamplerJobletVacuumJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyDayAfter(
						LocalTime.of(0, 0, 0),
						Duration.ofHours(4),
						serviceNameSupplier.get(),
						"TableStorageSummaryJob"),
				settings.runTableStorageSummaryJob,
				TableStorageSummaryJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyMinute(serviceNameSupplier.get(), "NodewatchCostMonitoringJob"),
				settings.runNodewatchCostMonitoringJob,
				NodewatchCostMonitoringJob.class,
				true);
	}

}
