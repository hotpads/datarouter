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
package io.datarouter.clustersetting.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.ClusterSettingFinder;
import io.datarouter.clustersetting.ClusterSettingScope;
import io.datarouter.clustersetting.ClusterSettingValidity;
import io.datarouter.clustersetting.config.DatarouterClusterSettingRoot;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSettingKey;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao;
import io.datarouter.clustersetting.storage.clustersettinglog.DatarouterClusterSettingLogDao;
import io.datarouter.clustersetting.web.dto.ClusterSettingAndValidityJspDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.environment.DatarouterEnvironmentType;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.storage.setting.DefaultSettingValue;
import io.datarouter.storage.setting.SettingRoot.SettingRootFinder;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.storage.util.KeyRangeTool;
import io.datarouter.util.BooleanTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Range;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;

@Singleton
public class ClusterSettingService{

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterClusterSettingRoot clusterSettingRoot;
	@Inject
	private DatarouterClusterSettingDao clusterSettingDao;
	@Inject
	private DatarouterClusterSettingLogDao clusterSettingLogDao;
	@Inject
	private DatarouterWebappInstanceDao webappInstanceDao;
	@Inject
	private ClusterSettingFinder clusterSettingFinder;
	@Inject
	private SettingRootFinder settingRootFinder;
	@Inject
	private ServerTypes serverTypes;

	public <T> T getSettingValueForWebappInstance(CachedSetting<T> memorySetting, WebappInstance webappInstance){
		// try database first
		List<ClusterSetting> settingsWithName = clusterSettingFinder.getAllSettingsWithName(memorySetting.getName());
		List<ClusterSetting> settingsForWebappInstance = ClusterSetting.filterForWebappInstance(settingsWithName,
				webappInstance);
		Optional<ClusterSetting> mostSpecificSetting = ClusterSetting.getMostSpecificSetting(settingsForWebappInstance);
		if(mostSpecificSetting.isPresent()){
			return ClusterSetting.getTypedValueOrUseDefaultFrom(mostSpecificSetting, memorySetting);
		}
		// use default
		var environmentType = new DatarouterEnvironmentType(datarouterProperties.getEnvironmentType());
		DefaultSettingValue<T> defaultSettingValue = memorySetting.getDefaultSettingValue();
		ServerType webAppInstanceServerType = serverTypes.fromPersistentString(webappInstance.getServerType());
		String serverName = datarouterProperties.getServerName();
		String environmentName = datarouterProperties.getEnvironment();
		return defaultSettingValue.getValue(environmentType, environmentName, webAppInstanceServerType, serverName);
	}

	public <T> Map<WebappInstance,T> getSettingValueByWebappInstance(CachedSetting<T> memorySetting){
		return webappInstanceDao.scan()
				.toMap(Function.identity(),
						instance -> getSettingValueForWebappInstance(memorySetting, instance));
	}

	public Scanner<ClusterSetting> streamWithValidity(ClusterSettingValidity validity){
		Map<String,String> serverTypeByServerName = webappInstanceDao.getServerTypeByServerName();
		return clusterSettingDao.scan()
				.exclude(setting -> setting.getName().startsWith("datarouter"))
				.include(setting -> getValidity(serverTypeByServerName, setting) == validity);
	}

	public Scanner<ClusterSettingAndValidityJspDto> scanClusterSettingAndValidityWithPrefix(String prefix){
		Map<String,String> serverTypeByServerName = webappInstanceDao.getServerTypeByServerName();
		Range<ClusterSettingKey> range = prefix == null
				? Range.everything()
				: KeyRangeTool.forPrefixWithWildcard(
						prefix,
						value -> new ClusterSettingKey(value, null, null, null, null));
		return clusterSettingDao.scan(range)
				.map(setting -> new ClusterSettingAndValidityJspDto(setting, getValidity(serverTypeByServerName,
						setting)));
	}

	private ClusterSettingValidity getValidity(
			Map<String,String> serverTypeByServerName,
			ClusterSetting databeanSetting){
		String name = databeanSetting.getName();
		ClusterSettingScope scope = databeanSetting.getScope();

		if(!settingRootFinder.isRecognized(name)){
			return ClusterSettingValidity.UNKNOWN;
		}

		Optional<CachedSetting<?>> memorySetting = settingRootFinder.getSettingByName(name);
		if(memorySetting.isEmpty()){
			return ClusterSettingValidity.EXPIRED;
		}

		String serverType = null;
		String serverName = databeanSetting.getServerName();
		if(scope == ClusterSettingScope.SERVER_TYPE){
			serverType = databeanSetting.getServerType();
		}else if(scope == ClusterSettingScope.SERVER_NAME){
			serverType = serverTypeByServerName.get(serverName);
		}

		//TODO check for INVALID_SERVER_TYPE

		if(scope == ClusterSettingScope.SERVER_NAME){
			if(StringTool.isEmpty(serverType)){
				return ClusterSettingValidity.INVALID_SERVER_NAME;
			}
		}

		var environmentType = new DatarouterEnvironmentType(datarouterProperties.getEnvironmentType());
		DefaultSettingValue<?> defaultSettingValue = memorySetting.get().getDefaultSettingValue();
		String environmentName = datarouterProperties.getEnvironment();
		ServerType currentServerType = datarouterProperties.getServerType();
		String currentServerName = datarouterProperties.getServerName();
		Object defaultValue = defaultSettingValue.getValue(environmentType, environmentName, currentServerType,
				currentServerName);
		Object databeanValue = databeanSetting.getTypedValue(memorySetting.get());
		boolean redundant = Objects.equals(defaultValue, databeanValue);

		if(redundant){
			return ClusterSettingValidity.REDUNDANT;
		}

		if(scope != ClusterSettingScope.APPLICATION){
			int oldSettingAlertThresholdDays = clusterSettingRoot.oldSettingAlertThresholdDays.get();
			Set<String> settingsExcludedFromOldSettingsAlert = clusterSettingRoot.settingsExcludedFromOldSettingsAlert
					.get();
			if(settingsExcludedFromOldSettingsAlert.stream()
					.noneMatch(setting -> StringTool.containsCaseInsensitive(name, setting))){
						if(clusterSettingLogDao.isOldDatabaseSetting(databeanSetting, oldSettingAlertThresholdDays)){
							return ClusterSettingValidity.OLD;
						}
			}
		}

		return ClusterSettingValidity.VALID;
	}

	public Optional<ClusterSetting> getSettingByName(String name){
		var prefix = new ClusterSettingKey(name, null, null, null, null);
		return clusterSettingDao.scanWithPrefix(prefix)
				.findFirst();
	}

	public String checkValidJobSettingOnAnyServerType(CachedSetting<Boolean> setting){
		Map<WebappInstance,Boolean> allSettings = getSettingValueByWebappInstance(setting);
		Optional<Boolean> validSetting = allSettings.values().stream()
				.filter(BooleanTool::isTrue)
				.findFirst();
		if(validSetting.isPresent()){
			return "";
		}
		return setting.getName() + " is not enabled on at least one server of any type";
	}

	public Integer checkValidJobletSettingOnAnyServerType(CachedSetting<Integer> setting){
		Map<WebappInstance,Integer> allSettings = getSettingValueByWebappInstance(setting);
		return allSettings.values().stream()
				.filter(value -> value != 0)
				.findFirst()
				.orElse(-1);
	}

}
