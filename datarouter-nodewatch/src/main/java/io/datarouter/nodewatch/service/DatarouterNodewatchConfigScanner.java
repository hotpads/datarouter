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
package io.datarouter.nodewatch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.service.ClusterSettingService;
import io.datarouter.joblet.setting.DatarouterJobletSettingRoot;
import io.datarouter.nodewatch.config.DatarouterNodewatchSettingRoot;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJoblet;
import io.datarouter.nodewatch.storage.latesttablecount.DatarouterLatestTableCountDao;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.autoconfig.ConfigScanDto;
import io.datarouter.web.autoconfig.ConfigScanResponseTool;

@Singleton
public class DatarouterNodewatchConfigScanner{

	@Inject
	private DatarouterNodewatchSettingRoot nodewatchSettings;
	@Inject
	private DatarouterLatestTableCountDao latestTableCountDao;
	@Inject
	private DatarouterJobletSettingRoot jobletSettings;
	@Inject
	private ClusterSettingService clusterSettingService;

	public ConfigScanDto checkRequiredNodewatchSettings(){
		List<String> responses = new ArrayList<>();

		// nodewatch jobs enabled and joblet
		Stream.of(nodewatchSettings.tableSamplerJob,
				nodewatchSettings.tableCountJob,
				nodewatchSettings.tableSizeMonitoringJob,
				jobletSettings.runJoblets)
				.map(clusterSettingService::checkValidJobSettingOnAnyServerType)
				.filter(StringTool::notNullNorEmptyNorWhitespace)
				.forEach(responses::add);

		// thread counts
		CachedSetting<Integer> clusterThreadCountSetting = jobletSettings
				.getCachedSettingClusterThreadCountForJobletType(TableSpanSamplerJoblet.JOBLET_TYPE);
		CachedSetting<Integer> threadCountSetting = jobletSettings.getCachedSettingThreadCountForJobletType(
				TableSpanSamplerJoblet.JOBLET_TYPE);

		int clusterThreadCount = clusterSettingService.checkValidJobletSettingOnAnyServerType(
				clusterThreadCountSetting);
		int threadCount = clusterSettingService.checkValidJobletSettingOnAnyServerType(threadCountSetting);
		if(clusterThreadCount < 2){
			responses.add("clusterThreadCount should be greater than 1");
		}
		if(threadCount < 1){
			responses.add("threadCount should be at least 1");
		}

		if(CollectionTool.isEmpty(responses)){
			return ConfigScanResponseTool.buildEmptyResponse();
		}
		String header = "TableCount cluster setting configuration";
		return ConfigScanResponseTool.buildResponse(header, responses);
	}

	// check that at least one table has row count greater than 1
	public ConfigScanDto checkLatestTableMinimumCount(){
		boolean anyTablesWithMinimumCount = latestTableCountDao.scan()
				.anyMatch(tableCount -> tableCount.getNumRows() >= 1);
		if(anyTablesWithMinimumCount){
			return ConfigScanResponseTool.buildEmptyResponse();
		}
		String header = "All LatestTableCounts have a row count of 0";
		return ConfigScanResponseTool.buildResponse(header);
	}

}
