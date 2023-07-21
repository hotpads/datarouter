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
package io.datarouter.websocket.config;

import io.datarouter.storage.config.environment.EnvironmentType;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterWebSocketSettingRoot extends SettingRoot{

	public final Setting<Boolean> testConnectionWithPing;
	public final Setting<Boolean> runWebSocketSessionVacuumJob;
	public final Setting<Boolean> runWebSocketSessionDatabaseMonitoringJob;
	public final Setting<Boolean> runWebSocketSessionMemoryMonitoringJob;

	@Inject
	public DatarouterWebSocketSettingRoot(SettingFinder finder, ServerTypes serverTypes){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterWebSocket.");

		testConnectionWithPing = registerBooleans("testConnectionWithPing", defaultTo(true));
		runWebSocketSessionVacuumJob = registerBooleans("runWebSocketSessionVacuumJob", defaultTo(false)
				.withServerType(EnvironmentType.PRODUCTION, serverTypes.getJobServerType(), true)
				.withEnvironmentType(EnvironmentType.DEVELOPMENT, true));
		runWebSocketSessionDatabaseMonitoringJob = registerBoolean("runWebSocketSessionDatabaseMonitoringJob", true);
		runWebSocketSessionMemoryMonitoringJob = registerBoolean("runWebSocketSessionMemoryMonitoringJob", true);
	}

}
