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

import io.datarouter.joblet.setting.DatarouterJobletClusterThreadCountSettings;
import io.datarouter.joblet.setting.DatarouterJobletThreadCountSettings;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.storage.config.setting.DatarouterSettingOverrides;
import io.datarouter.websocket.job.WebSocketSessionVacuumJoblet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class WebSocketSettingOverrides implements DatarouterSettingOverrides{

	@Inject
	private DatarouterJobletThreadCountSettings jobletThreadCountSettings;
	@Inject
	private DatarouterJobletClusterThreadCountSettings jobletClusterThreadCountSettings;

	// invoke me
	@Inject
	private void enableJobletTypeProduction(){
		JobletType<?> jobletType = WebSocketSessionVacuumJoblet.JOBLET_TYPE;
		jobletClusterThreadCountSettings.getSettingForJobletType(jobletType).setGlobalDefault(10);
		jobletThreadCountSettings.getSettingForJobletType(jobletType).setGlobalDefault(3);
	}

}
