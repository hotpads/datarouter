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
package io.datarouter.joblet.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.service.ClusterSettingService;
import io.datarouter.joblet.setting.DatarouterJobletThreadCountSettings;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;

@Singleton
public class ActiveJobletTypeFactory{

	@Inject
	private JobletTypeFactory jobletTypeFactory;
	@Inject
	private DatarouterJobletThreadCountSettings jobletThreadCountSettings;
	@Inject
	private ClusterSettingService clusterSettingService;

	public List<JobletType<?>> getAllActiveTypes(){
		return getActiveTypesFrom(jobletTypeFactory.getAllTypes());
	}

	public List<JobletType<?>> getActiveTypesCausingScaling(){
		return getActiveTypesFrom(jobletTypeFactory.getTypesCausingScaling());
	}

	private List<JobletType<?>> getActiveTypesFrom(Collection<JobletType<?>> typesToConsider){
		List<JobletType<?>> activeTypes = new ArrayList<>();
		for(JobletType<?> type : typesToConsider){
			CachedSetting<Integer> setting = jobletThreadCountSettings.getSettingForJobletType(type);
			Map<WebappInstance,Integer> threadCountByWebAppInstance = clusterSettingService
					.getSettingValueByWebappInstance(setting);
			Optional<Integer> anyPositiveThreadCount = threadCountByWebAppInstance.values().stream()
					.filter(threadCount -> threadCount > 0)
					.findAny();
			if(anyPositiveThreadCount.isPresent()){
				activeTypes.add(type);
			}
		}
		return activeTypes;
	}

}
