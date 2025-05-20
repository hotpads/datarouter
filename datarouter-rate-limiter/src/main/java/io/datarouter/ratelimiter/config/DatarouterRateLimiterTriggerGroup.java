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
package io.datarouter.ratelimiter.config;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.util.DatarouterCronTool;
import io.datarouter.ratelimiter.DatarouterRateLimiterTtlVacuumJob;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.time.ZoneIds;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterRateLimiterTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterRateLimiterTriggerGroup(
			ServiceName serviceNameSupplier,
			DatarouterRateLimiterSettings settings){
		super("DatarouterRateLimiter", Tag.DATAROUTER, ZoneIds.AMERICA_NEW_YORK);
		registerLocked(
				DatarouterCronTool.everyNHours(4, serviceNameSupplier.get(), "DatarouterRateLimiterTtlVacuumJob"),
				settings.runTtlVacuum,
				DatarouterRateLimiterTtlVacuumJob.class,
				true);
	}

}
