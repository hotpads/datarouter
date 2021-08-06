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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.util.time.ZoneIds;
import io.datarouter.websocket.job.WebSocketSessionVacuumJob;

@Singleton
public class DatarouterWebSocketTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterWebSocketTriggerGroup(DatarouterWebSocketSettingRoot settings){
		super("DatarouterWebSocket", true, ZoneIds.AMERICA_NEW_YORK);
		registerLocked(
				// High frequency to keep the table as consistent as possible after some sessions failed to delete
				"1/30 * * * * ?",
				settings.runWebSocketSessionVacuumJob,
				WebSocketSessionVacuumJob.class,
				true);
	}

}
