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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.loggerconfig.job.LoggerConfigCleanupJob;
import io.datarouter.loggerconfig.job.LoggerConfigUpdaterJob;
import io.datarouter.util.time.ZoneIds;

@Singleton
public class DatarouterLoggerConfigTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterLoggerConfigTriggerGroup(DatarouterLoggerConfigSettingRoot settings){
		super("DatarouterLoggerConfig", true, ZoneIds.AMERICA_NEW_YORK);
		registerParallel(
				"0/15 * * * * ?",
				settings.runLoggerConfigUpdaterJob,
				LoggerConfigUpdaterJob.class);
		registerLocked(
				"14 5 15 ? * MON-FRI *",
				settings.runLoggerConfigCleanupJob,
				LoggerConfigCleanupJob.class,
				true);
	}

}
