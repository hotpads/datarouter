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
package io.datarouter.clustersetting.service;

import java.util.Optional;

import io.datarouter.clustersetting.web.browse.ClusterSettingHierarchy;
import io.datarouter.storage.setting.cached.CachedSetting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ClusterSettingValidationService{

	@Inject
	private ClusterSettingHierarchy hierarchy;

	public Optional<String> findErrorForSettingValue(String settingName, String settingValue){
		Optional<CachedSetting<?>> optSetting = hierarchy.root().findSetting(settingName);
		if(optSetting.isEmpty()){
			return Optional.empty();
		}
		CachedSetting<?> setting = optSetting.orElseThrow();
		if(setting.isValid(settingValue)){
			return Optional.empty();
		}
		String message = String.format(
				"Invalid value for %s",
				setting.getClass().getSimpleName());
		return Optional.of(message);
	}

}
