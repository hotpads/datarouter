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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.time.ZoneIds;
import io.datarouter.webappinstance.job.DeadClusterJobLockVacuumJob;
import io.datarouter.webappinstance.job.OneTimeLoginTokenVacuumJob;
import io.datarouter.webappinstance.job.WebappInstanceAlertJob;
import io.datarouter.webappinstance.job.WebappInstanceUpdateJob;
import io.datarouter.webappinstance.job.WebappInstanceVacuumJob;

@Singleton
public class DatarouterWebappInstanceTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterWebappInstanceTriggerGroup(DatarouterWebappInstanceSettingRoot settings){
		super("DatarouterWebappInstance", Tag.DATAROUTER, ZoneIds.AMERICA_NEW_YORK);
		registerParallel(
				"0/" + WebappInstanceUpdateJob.WEBAPP_INSTANCE_UPDATE_SECONDS_DELAY + " * * * * ?",
				() -> true,
				WebappInstanceUpdateJob.class);
		registerParallel(
				"0 0 15-21 ? * MON,TUE,WED,THU,FRI *",
				settings.alertOnStaleWebappInstance,
				WebappInstanceAlertJob.class);
		registerLocked(
				"43 3/10 * * * ?",
				settings.runWebappInstanceVacuumJob,
				WebappInstanceVacuumJob.class,
				true);
		registerLocked(
				"23 0 0 * * ?",
				settings.runOneTimeLoginTokenVacuumJob,
				OneTimeLoginTokenVacuumJob.class,
				true);
		registerParallel(
				"0 10/20 * * * ?",
				settings.runDeadClusterJobLockVacuumJob,
				DeadClusterJobLockVacuumJob.class);
	}

}
