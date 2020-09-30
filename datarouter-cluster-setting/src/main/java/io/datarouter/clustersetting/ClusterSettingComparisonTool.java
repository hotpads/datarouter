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
package io.datarouter.clustersetting;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.setting.Setting;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;

public class ClusterSettingComparisonTool{

	public static Optional<ClusterSetting> getMostSpecificSettingForWebappInstance(
			List<ClusterSetting> settings,
			WebappInstance webappInstance){
		List<ClusterSetting> settingsForWebappInstance = appliesToWebappInstance(settings, webappInstance);
		return getMostSpecificSetting(settingsForWebappInstance);
	}

	public static Optional<ClusterSetting> getMostSpecificSetting(List<ClusterSetting> settings){
		return settings.isEmpty()
				? Optional.empty()
				: Optional.of(Collections.min(settings, new ClusterSettingScopeComparator()));
	}

	public static List<ClusterSetting> appliesToWebappInstance(
			List<ClusterSetting> settings,
			WebappInstance webappInstance){
		return Scanner.of(settings)
				.include(setting -> setting.getKey().appliesToWebappInstance(webappInstance))
				.list();
	}

	public static <T> T getTypedValueOrUseDefaultFrom(
			Optional<ClusterSetting> clusterSetting,
			Setting<T> settingForTypeAndDefault){
		return clusterSetting
				.map(setting -> setting.getTypedValue(settingForTypeAndDefault))
				.orElseGet(settingForTypeAndDefault::getDefaultValue);
	}

	public static boolean equal(ClusterSetting first, ClusterSetting second){
		if(ObjectTool.bothNull(first, second)){
			return true;
		}
		if(ObjectTool.isOneNullButNotTheOther(first, second)){
			return false;
		}
		return first.getName().equals(second.getName())
				&& first.getScope() == second.getScope()
				&& first.getServerType().equals(second.getServerType())
				&& first.getServerName().equals(second.getServerName())
				&& first.getValue().equals(second.getValue());
	}

}
