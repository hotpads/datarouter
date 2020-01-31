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
package io.datarouter.clustersetting.report;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.service.ClusterSettingService;
import io.datarouter.job.config.DatarouterJobSettingRoot;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.autoconfig.ConfigScanDto;
import io.datarouter.web.autoconfig.ConfigScanResponseTool;

@Singleton
public class DatarouterJobConfigScanner{

	@Inject
	private DatarouterJobSettingRoot jobSettings;
	@Inject
	private ClusterSettingService clusterSettingService;

	public ConfigScanDto checkLongRunningTaskVacuumJobEnabled(){
		String response = clusterSettingService.checkValidJobSettingOnAnyServerType(
				jobSettings.runLongRunningTaskVacuum);
		if(StringTool.isNullOrEmptyOrWhitespace(response)){
			return ConfigScanResponseTool.buildEmptyResponse();
		}
		return ConfigScanResponseTool.buildResponse(response);
	}

}
