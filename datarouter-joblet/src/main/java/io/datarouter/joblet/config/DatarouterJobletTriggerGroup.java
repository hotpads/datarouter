/**
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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.joblet.job.JobletCounterJob;
import io.datarouter.joblet.job.JobletDataVacuumJob;
import io.datarouter.joblet.job.JobletInstanceCounterJob;
import io.datarouter.joblet.job.JobletRequeueJob;
import io.datarouter.joblet.job.JobletVacuumJob;
import io.datarouter.joblet.setting.DatarouterJobletSettingRoot;

@Singleton
public class DatarouterJobletTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterJobletTriggerGroup(DatarouterJobletSettingRoot settings){
		super("DatarouterJoblet", true);
		registerLocked(
				"26 2/5 * * * ?",
				settings.runJobletCounterJob,
				JobletCounterJob.class,
				true);
		registerLocked(
				"5 0/5 * * * ?",
				settings.runJobletRequeueJob,
				JobletRequeueJob.class,
				true);
		registerLocked(
				"0 15 13 * * ?",
				settings.runJobletVacuum,
				JobletVacuumJob.class,
				true);
		registerLocked(
				"0 0 15 * * ?",
				settings.runJobletDataVacuum,
				JobletDataVacuumJob.class,
				true);
		registerLocked(
				"12/30 * * * * ?",
				settings.runJobletInstanceCounterJob,
				JobletInstanceCounterJob.class,
				true);
	}

}
