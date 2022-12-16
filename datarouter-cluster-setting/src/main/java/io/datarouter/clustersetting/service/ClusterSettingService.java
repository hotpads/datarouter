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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.ClusterSettingComparisonTool;
import io.datarouter.clustersetting.ClusterSettingFinder;
import io.datarouter.clustersetting.ClusterSettingScope;
import io.datarouter.clustersetting.ClusterSettingScopeComparator;
import io.datarouter.clustersetting.ClusterSettingValidity;
import io.datarouter.clustersetting.config.DatarouterClusterSettingRoot;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSettingKey;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao;
import io.datarouter.clustersetting.storage.clustersettinglog.DatarouterClusterSettingLogDao;
import io.datarouter.clustersetting.web.dto.ClusterSettingAndValidityJspDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.DatarouterEnvironmentTypeSupplier;
import io.datarouter.storage.config.properties.DatarouterServerTypeSupplier;
import io.datarouter.storage.config.properties.EnvironmentCategoryName;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.storage.setting.DatarouterSettingTag;
import io.datarouter.storage.setting.DefaultSettingValue;
import io.datarouter.storage.setting.SettingRoot.SettingRootFinder;
import io.datarouter.storage.setting.cached.CachedClusterSettingTags;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.storage.util.KeyRangeTool;
import io.datarouter.util.BooleanTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Range;
import io.datarouter.webappinstance.service.WebappInstanceService;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;

@Singleton
public class ClusterSettingService{

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
	@Inject
	private WebappInstanceService webappInstanceService;
	@Inject
	private CachedClusterSettingTags cachedClusterSettingTags;
	@Inject
	private ServerName serverName;
	@Inject
	private ServiceName serviceName;
	@Inject
	private EnvironmentName environmentName;
	@Inject
	private EnvironmentCategoryName environmentCategoryName;
	@Inject
	private DatarouterServerTypeSupplier serverTypeSupplier;
	@Inject
	private DatarouterEnvironmentTypeSupplier environmentTypeSupplier;

	public <T> T getSettingValueForWebappInstance(CachedSetting<T> memorySetting, WebappInstance webappInstance){
		// try database first
		List<ClusterSetting> settingsWithName = clusterSettingFinder.getAllSettingsWithName(memorySetting.getName());
		Optional<ClusterSetting> mostSpecificSetting = ClusterSettingComparisonTool
				.getMostSpecificSettingForWebappInstance(settingsWithName, webappInstance);
		if(mostSpecificSetting.isPresent()){
			return ClusterSettingComparisonTool.getTypedValueOrUseDefaultFrom(mostSpecificSetting, memorySetting);
		}
		// use default
		var environmentType = environmentTypeSupplier.getDatarouterEnvironmentType();
		DefaultSettingValue<T> defaultSettingValue = memorySetting.getDefaultSettingValue();
		ServerType serverType = serverTypes.fromPersistentString(webappInstance.getServerType());
		List<DatarouterSettingTag> settingTags = cachedClusterSettingTags.get();
		return defaultSettingValue.getValue(
				environmentType,
				environmentCategoryName.get(),
				environmentName.get(),
				serverType,
				serviceName.get(),
				serverName.get(),
				settingTags);
	}

	public <T> Map<WebappInstance,T> getSettingValueByWebappInstance(CachedSetting<T> memorySetting){
		return webappInstanceDao.scan()
				.toMap(Function.identity(), instance -> getSettingValueForWebappInstance(memorySetting, instance));
	}

	public Scanner<ClusterSetting> scanWithValidity(ClusterSettingValidity validity){
		WebappInstance currentWebappInstance = webappInstanceDao.get(webappInstanceService
				.buildCurrentWebappInstanceKey());
		return clusterSettingDao.scan()
				.exclude(setting -> setting.getName().startsWith("datarouter"))
				.include(setting -> getValidityForWebappInstance(setting, currentWebappInstance) == validity);
	}

	public Scanner<ClusterSetting> scanAllWebappInstancesWithRedundantValidity(){
		List<WebappInstance> allWebappInstances = webappInstanceDao.scan().list();
		return clusterSettingDao.scan()
				.exclude(setting -> setting.getName().startsWith("datarouter"))
				.include(setting -> Scanner.of(allWebappInstances)
						.allMatch(webappInstance -> getValidityForWebappInstance(setting, webappInstance)
								== ClusterSettingValidity.REDUNDANT));
	}

	public Optional<ClusterSetting> getMostSpecificClusterSetting(List<ClusterSetting> dbSettings){
		WebappInstance currentWebappInstance = webappInstanceDao.get(webappInstanceService
				.buildCurrentWebappInstanceKey());
		return ClusterSettingComparisonTool.getMostSpecificSettingForWebappInstance(dbSettings, currentWebappInstance);
	}

	public Scanner<ClusterSettingAndValidityJspDto> scanClusterSettingAndValidityWithPrefix(String prefix){
		WebappInstance currentWebappInstance = webappInstanceDao.get(webappInstanceService
				.buildCurrentWebappInstanceKey());
		Range<ClusterSettingKey> range = prefix == null
				? Range.everything()
				: KeyRangeTool.forPrefixWithWildcard(
						prefix,
						name -> new ClusterSettingKey(name, null, null, null));
		return clusterSettingDao.scan(range)
				.map(setting -> {
					ClusterSettingValidity validity = getValidityForWebappInstance(setting, currentWebappInstance);
					return new ClusterSettingAndValidityJspDto(setting, validity);
				});
	}

	private ClusterSettingValidity getValidityForWebappInstance(ClusterSetting databeanSetting,
			WebappInstance webappInstance){
		String name = databeanSetting.getName();
		ClusterSettingScope scope = databeanSetting.getScope();

		if(!settingRootFinder.isRecognized(name)){
			if(clusterSettingRoot.settingsExcludedFromUnknownSettingsAlert.get().stream()
					.anyMatch(setting -> StringTool.containsCaseInsensitive(name, setting))){
				return ClusterSettingValidity.VALID;
			}
			return ClusterSettingValidity.UNKNOWN;
		}

		CachedSetting<?> memorySetting = settingRootFinder.getSettingByName(name).orElse(null);
		if(memorySetting == null){
			return ClusterSettingValidity.UNREFERENCED;
		}

		if(scope == ClusterSettingScope.SERVER_TYPE){
			String serverType = databeanSetting.getServerType();
			try{
				serverTypes.fromPersistentString(serverType);
			}catch(RuntimeException e){
				return ClusterSettingValidity.INVALID_SERVER_TYPE;
			}
		}

		if(scope == ClusterSettingScope.SERVER_NAME){
			String serverName = databeanSetting.getServerName();
			String serverTypeFromWebappInstanceDao = webappInstanceDao.getServerTypeByServerName().get(serverName);
			if(StringTool.isEmpty(serverTypeFromWebappInstanceDao)){
				return ClusterSettingValidity.INVALID_SERVER_NAME;
			}
		}

		if(isClusterSettingRedundantForWebappInstance(memorySetting, databeanSetting, webappInstance)){
			return ClusterSettingValidity.REDUNDANT;
		}

		int oldSettingAlertThresholdDays = clusterSettingRoot.oldSettingAlertThresholdDays.get();
		if(clusterSettingLogDao.isOldDatabaseSetting(databeanSetting, oldSettingAlertThresholdDays)){
			if(clusterSettingRoot.isExcludedOldSettingString(name)){
				return ClusterSettingValidity.VALID;
			}
			return ClusterSettingValidity.OLD;
		}
		return ClusterSettingValidity.VALID;
	}

	private boolean isClusterSettingRedundantForWebappInstance(
			CachedSetting<?> memorySetting,
			ClusterSetting databeanSetting,
			WebappInstance webappInstance){
		// get all db setting overrides with name
		List<ClusterSetting> databeanSettings = clusterSettingFinder.getAllSettingsWithName(memorySetting.getName());
		// filter for settings that apply only for current webapp instance
		List<ClusterSetting> appliesToWebappInstance = ClusterSettingComparisonTool
				.appliesToWebappInstance(databeanSettings, webappInstance);
		// do not check for redundancy if setting does not apply to current webapp instance
		if(!appliesToWebappInstance.contains(databeanSetting)){
			return false;
		}
		// necessary sort
		appliesToWebappInstance.sort(new ClusterSettingScopeComparator().reversed());

		// check if all settings in the chain have the same value, up to the current databean setting
		boolean allMatch = Scanner.of(appliesToWebappInstance)
				.advanceUntil(setting -> ClusterSettingComparisonTool.equal(databeanSetting, setting))
				.allMatch(setting -> setting.getValue().equals(databeanSetting.getValue()));

		// only compare with the code default if all databean setting values in the evaluation chain match
		if(allMatch){
			DefaultSettingValue<?> defaultSettingValue = memorySetting.getDefaultSettingValue();
			Object defaultValue = defaultSettingValue.getValue(
					environmentTypeSupplier.getDatarouterEnvironmentType(),
					environmentCategoryName.get(),
					environmentName.get(),
					serverTypeSupplier.get(),
					serviceName.get(),
					serverName.get(),
					cachedClusterSettingTags.get());
			Object databeanSettingValue = databeanSetting.getTypedValue(memorySetting);
			return Objects.equals(defaultValue, databeanSettingValue);
		}
		return false;
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
