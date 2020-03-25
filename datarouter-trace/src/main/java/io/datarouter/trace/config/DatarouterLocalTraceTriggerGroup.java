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
package io.datarouter.trace.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.trace.job.TraceSpanVacuumJob;
import io.datarouter.trace.job.TraceThreadVacuumJob;
import io.datarouter.trace.job.TraceVacuumJob;
import io.datarouter.trace.settings.DatarouterTraceLocalSettingRoot;

@Singleton
public class DatarouterLocalTraceTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterLocalTraceTriggerGroup(DatarouterTraceLocalSettingRoot settings){
		super("DatarouterTrace");
		registerLocked(
				"3 15 0/2 ? * *",
				settings.runVacuumJob,
				TraceVacuumJob.class,
				true);
		registerLocked(
				"3 20 0/2 ? * *",
				settings.runVacuumJob,
				TraceThreadVacuumJob.class,
				true);
		registerLocked(
				"3 25 0/2 ? * *",
				settings.runVacuumJob,
				TraceSpanVacuumJob.class,
				true);
	}

}
