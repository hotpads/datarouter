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
package io.datarouter.clustersetting.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.job.ClusterSettingCacheRefreshJob;
import io.datarouter.clustersetting.job.ClusterSettingConfigurationScanJob;
import io.datarouter.clustersetting.job.LongRunningTaskConfigurationScanJob;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.time.ZoneIds;

@Singleton
public class DatarouterClusterSettingTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterClusterSettingTriggerGroup(DatarouterClusterSettingRoot settings){
		super("DatarouterClusterSetting", Tag.DATAROUTER, ZoneIds.AMERICA_NEW_YORK);
		registerParallel(
				"5/10 * * * * ?",
				() -> true,
				ClusterSettingCacheRefreshJob.class);
		registerLocked(
				"0 0 14 ? * MON,TUE,WED,THU,FRI *",
				settings.runConfigurationScanReportEmailJob,
				LongRunningTaskConfigurationScanJob.class,
				true);
		registerLocked(
				"0 0 14 ? * MON,TUE,WED,THU,FRI *",
				settings.runConfigurationScanReportEmailJob,
				ClusterSettingConfigurationScanJob.class,
				true);
	}

}
