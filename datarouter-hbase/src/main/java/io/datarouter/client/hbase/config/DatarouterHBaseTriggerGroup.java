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
package io.datarouter.client.hbase.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.hbase.balancer.HBaseRegionBalancerJob;
import io.datarouter.client.hbase.compaction.HBaseCompactionInfo;
import io.datarouter.client.hbase.compaction.HBaseCompactionJob;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.util.time.ZoneIds;

@Singleton
public class DatarouterHBaseTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterHBaseTriggerGroup(DatarouterHBaseSettingRoot settings, HBaseCompactionInfo compactionInfo){
		super("DatarouterHbase", true, ZoneIds.AMERICA_NEW_YORK);
		long compactTriggerPeriodMinutes = compactionInfo.getCompactionTriggerPeriod().toMinutes();
		registerLocked(
				"41 7/" + compactTriggerPeriodMinutes + " * * * ?",
				settings.runHbaseCompactionJob,
				HBaseCompactionJob.class,
				true);
		registerLocked(
				"30 12 * * * ? *",
				() -> settings.shouldRunHBaseRegionBalancerJob(),
				HBaseRegionBalancerJob.class,
				true);
	}

}
