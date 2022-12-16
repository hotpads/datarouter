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
package io.datarouter.clustersetting.web.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.storage.setting.DefaultSettingValue;
import io.datarouter.storage.setting.DefaultSettingValueWinner;
import io.datarouter.storage.setting.DefaultSettingValueWinner.DefaultSettingValueWinnerType;
import io.datarouter.storage.setting.cached.CachedSetting;

public class SettingJspDto{

	private final String name;
	private final Boolean hasRedundantCustomValue;
	private final Boolean hasCustomValue;
	private final String value;
	private final String defaultValue;
	private final boolean isGlobalDefault;
	private final List<ClusterSettingDefaultJspDto> codeOverrides;
	private final List<ClusterSettingTagJspDto> settingTags;

	public <T> SettingJspDto(CachedSetting<T> setting){
		this.name = setting.getName();
		this.hasRedundantCustomValue = setting.getHasRedundantCustomValue();
		this.hasCustomValue = setting.getHasCustomValue();
		this.value = setting.toStringValue();
		this.defaultValue = setting.toStringDefaultValue();
		DefaultSettingValueWinner defaultValueWinner = setting.getDefaultSettingValueWinner();
		this.isGlobalDefault = setting.getMostSpecificDatabeanValue().isEmpty()
				&& defaultValueWinner.type == DefaultSettingValueWinnerType.GLOBAL_DEFAULT;
		this.codeOverrides = toDefaults(setting, setting.getDefaultSettingValue(), defaultValueWinner);
		this.settingTags = toSettingTagJspDto(setting.getDefaultSettingValue(), defaultValueWinner);
	}

	private <T> List<ClusterSettingDefaultJspDto> toDefaults(CachedSetting<T> setting, DefaultSettingValue<T> defaults,
			DefaultSettingValueWinner defaultValueWinner){
		List<ClusterSettingDefaultJspDto> dtos = new ArrayList<>();
		String winnerEnvironmentType = defaultValueWinner.environmentType != null ? defaultValueWinner.environmentType
				: "";
		String winnerServerType = defaultValueWinner.serverType != null ? defaultValueWinner.serverType : "";
		String winnerServerName = defaultValueWinner.serverName != null ? defaultValueWinner.serverName : "";
		String winnerEnvironmentName = defaultValueWinner.environmentName != null ? defaultValueWinner.environmentName
				: "";
		// database overrides have higher priority than code overrides
		boolean hasDatabaseOverride = setting.getMostSpecificDatabeanValue().isPresent();
		// environmentType overrides
		defaults.getValueByEnvironmentType().forEach((environmentType, value) -> {
			boolean isActive = winnerEnvironmentType.equals(environmentType.getPersistentString());
			boolean isWinner = !hasDatabaseOverride && isActive
					&& defaultValueWinner.type == DefaultSettingValueWinnerType.ENVIRONMENT_TYPE;
				dtos.add(new ClusterSettingDefaultJspDto(
						false,
						environmentType.getPersistentString(),
						null,
						null,
						null,
						null,
						setting.toStringValue(value),
						isActive,
						isWinner));
				});
		// serverType overrides
		defaults.getValueByServerTypeByEnvironmentType().forEach((environmentType, defaultByServerType) ->
				defaultByServerType.forEach((serverType, value) -> {
					boolean isActive = winnerEnvironmentType.equals(environmentType.getPersistentString())
							&& winnerServerType.equals(serverType);
					boolean isWinner = !hasDatabaseOverride && isActive
							&& defaultValueWinner.type == DefaultSettingValueWinnerType.SERVER_TYPE;
					dtos.add(new ClusterSettingDefaultJspDto(
						false,
						environmentType.getPersistentString(),
						null,
						null,
						serverType,
						null,
						setting.toStringValue(value),
						isActive,
						isWinner));
					}));
		// serviceName overrides
		defaults.getValueByServiceNameByEnvironmentType().forEach((environmentType, defaultByServiceName) ->
				defaultByServiceName.forEach((serviceName, value) -> {
					boolean isActive = winnerEnvironmentType.equals(environmentType.getPersistentString())
						&& winnerServerName.equals(serviceName);
					boolean isWinner = !hasDatabaseOverride && isActive
						&& defaultValueWinner.type == DefaultSettingValueWinnerType.SERVICE_NAME;
					dtos.add(new ClusterSettingDefaultJspDto(
						false,
						environmentType.getPersistentString(),
						null,
						null,
						null,
						serviceName,
						setting.toStringValue(value),
						isActive,
						isWinner));
				}));
		// serverName overrides
		defaults.getValueByServerNameByEnvironmentType().forEach((environmentType, defaultByServerName) ->
				defaultByServerName.forEach((serverName, value) -> {
					boolean isActive = winnerEnvironmentType.equals(environmentType.getPersistentString())
							&& winnerServerName.equals(serverName);
					boolean isWinner = !hasDatabaseOverride && isActive
							&& defaultValueWinner.type == DefaultSettingValueWinnerType.SERVER_NAME;
					dtos.add(new ClusterSettingDefaultJspDto(
						false,
						environmentType.getPersistentString(),
						null,
						null,
						null,
						serverName,
						setting.toStringValue(value),
						isActive,
						isWinner));
					}));
		// environmentName overrides
		defaults.getValueByEnvironmentNameByEnvironmentType().forEach((environmentType, defaultByEnvironment) ->
				defaultByEnvironment.forEach((environmentName, value) -> {
					boolean isActive = !isGlobalDefault
							&& winnerEnvironmentType.equals(environmentType.getPersistentString())
							&& winnerEnvironmentName.equals(environmentName);
					boolean isWinner = !hasDatabaseOverride && isActive
							&& defaultValueWinner.type == DefaultSettingValueWinnerType.ENVIRONMENT_NAME;
					dtos.add(new ClusterSettingDefaultJspDto(
						false,
						environmentType.getPersistentString(),
						environmentName,
						null,
						null,
						null,
						setting.toStringValue(value),
						isActive,
						isWinner));
					}));
		// environmentCategory overrides
		defaults.getValueByEnvironmentCategoryNameByEnvironmentType()
				.forEach((environmentType, defaultByEnvironmentCategory) ->
					defaultByEnvironmentCategory.forEach((environmentCategory, value) -> {
					boolean isActive = !isGlobalDefault
						&& winnerEnvironmentType.equals(environmentType.getPersistentString())
						&& winnerEnvironmentName.equals(environmentCategory);
					boolean isWinner = !hasDatabaseOverride && isActive
						&& defaultValueWinner.type == DefaultSettingValueWinnerType.ENVIRONMENT_CATEGORY;
					dtos.add(new ClusterSettingDefaultJspDto(
						false,
						environmentType.getPersistentString(),
						null,
						environmentCategory,
						null,
						null,
						setting.toStringValue(value),
						isActive,
						isWinner));
				}));
		return dtos;
	}

	private <T> List<ClusterSettingTagJspDto> toSettingTagJspDto(DefaultSettingValue<T> defaults,
			DefaultSettingValueWinner defaultValueWinner){
		return defaults.getValueBySettingTag().entrySet().stream()
				.map(tag -> {
					boolean isWinner = defaultValueWinner.type == DefaultSettingValueWinnerType.SETTING_TAG
							&& defaultValueWinner.settingTag.equals(tag.getKey().getPersistentString());
					return new ClusterSettingTagJspDto(
							tag.getKey().getPersistentString(),
							String.valueOf(tag.getValue().get()),
							isWinner);
				})
				.collect(Collectors.toList());

	}

	public String getName(){
		return name;
	}

	public Boolean getHasRedundantCustomValue(){
		return hasRedundantCustomValue;
	}

	public Boolean getHasCustomValue(){
		return hasCustomValue;
	}

	public String getValue(){
		return value;
	}

	public String getDefaultValue(){
		return defaultValue;
	}

	public boolean getIsGlobalDefault(){
		return isGlobalDefault;
	}

	public List<ClusterSettingDefaultJspDto> getCodeOverrides(){
		return codeOverrides;
	}

	public List<ClusterSettingTagJspDto> getSettingTags(){
		return settingTags;
	}

}
