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
package io.datarouter.client.mysql.config;

import io.datarouter.client.mysql.job.FastMysqlLiveTableOptionsRefresherJob;
import io.datarouter.client.mysql.job.MysqlLiveTableOptionsRefresherJob;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.util.DatarouterCronTool;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.time.ZoneIds;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterMysqlTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterMysqlTriggerGroup(
			ServerName serverNameSupplier,
			DatarouterMysqlSettingRoot settings){
		super("DatarouterMysql", Tag.DATAROUTER, ZoneIds.AMERICA_NEW_YORK);
		registerParallel(
				DatarouterCronTool.everyNSeconds(15, serverNameSupplier.get(), "MysqlLiveTableOptionsRefresherJob"),
				() -> settings.runFastMysqlLiveTableOptionsRefresherSpeed.get().equals("slow"),
				MysqlLiveTableOptionsRefresherJob.class);
		registerParallel(
				DatarouterCronTool.everySecond(),
				() -> settings.runFastMysqlLiveTableOptionsRefresherSpeed.get().equals("fast"),
				FastMysqlLiveTableOptionsRefresherJob.class);
	}

}
