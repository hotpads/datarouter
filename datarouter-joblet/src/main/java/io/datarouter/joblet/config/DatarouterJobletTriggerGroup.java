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
package io.datarouter.joblet.config;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.util.DatarouterCronTool;
import io.datarouter.joblet.job.JobletCounterJob;
import io.datarouter.joblet.job.JobletDataVacuumJob;
import io.datarouter.joblet.job.JobletInstanceCounterJob;
import io.datarouter.joblet.job.JobletRequeueJob;
import io.datarouter.joblet.job.JobletVacuumJob;
import io.datarouter.joblet.setting.DatarouterJobletSettingRoot;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.time.ZoneIds;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterJobletTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterJobletTriggerGroup(
			ServiceName serviceNameSupplier,
			DatarouterJobletSettingRoot settings){
		super("DatarouterJoblet", Tag.DATAROUTER, ZoneIds.AMERICA_NEW_YORK);
		registerLocked(
				DatarouterCronTool.everyNMinutes(5, serviceNameSupplier.get(), "JobletCounterJob"),
				settings.runJobletCounterJob,
				JobletCounterJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyNMinutes(5, serviceNameSupplier.get(), "JobletRequeueJob"),
				settings.runJobletRequeueJob,
				JobletRequeueJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyDay(serviceNameSupplier.get(), "JobletVacuumJob"),
				settings.runJobletVacuum,
				JobletVacuumJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyDay(serviceNameSupplier.get(), "JobletDataVacuumJob"),
				settings.runJobletDataVacuum,
				JobletDataVacuumJob.class,
				true);
		registerLocked(
				DatarouterCronTool.everyNSeconds(30, serviceNameSupplier.get(), "JobletInstanceCounterJob"),
				settings.runJobletInstanceCounterJob,
				JobletInstanceCounterJob.class,
				true);
	}

}
