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
package io.datarouter.client.mysql.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.job.FastMysqlLiveTableOptionsRefresherJob;
import io.datarouter.client.mysql.job.MysqlLiveTableOptionsRefresherJob;
import io.datarouter.job.BaseTriggerGroup;

@Singleton
public class DatarouterMysqlTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterMysqlTriggerGroup(DatarouterMysqlSettingRoot settings){
		super("DatarouterMysql", true);
		registerParallel(
				"5/15 * * * * ? *",
				() -> settings.runFastMysqlLiveTableOptionsRefresherSpeed.get().equals("slow"),
				MysqlLiveTableOptionsRefresherJob.class);
		registerParallel(
				"* * * * * ? *",
				() -> settings.runFastMysqlLiveTableOptionsRefresherSpeed.get().equals("fast"),
				FastMysqlLiveTableOptionsRefresherJob.class);
	}

}
