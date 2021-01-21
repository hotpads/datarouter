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
package io.datarouter.clustersetting.web.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
	private final List<ClusterSettingDefaultJspDto> codeOverrides;
	private final List<ClusterSettingTagJspDto> settingTags;

	public <T> SettingJspDto(CachedSetting<T> setting){
		this.name = setting.getName();
		this.hasRedundantCustomValue = setting.getHasRedundantCustomValue();
		this.hasCustomValue = setting.getHasCustomValue();
		this.value = setting.toStringValue();
		this.defaultValue = setting.toStringDefaultValue();
		Optional<DefaultSettingValueWinner> defaultValueWinner = setting.getDefaultSettingValueWinner();
		this.codeOverrides = toDefaults(setting, setting.getDefaultSettingValue(), defaultValueWinner);
		this.settingTags = toSettingTagJspDto(setting.getDefaultSettingValue(), defaultValueWinner);
	}

	private <T> List<ClusterSettingDefaultJspDto> toDefaults(CachedSetting<T> setting, DefaultSettingValue<T> defaults,
			Optional<DefaultSettingValueWinner> defaultValueWinner){
		List<ClusterSettingDefaultJspDto> dtos = new ArrayList<>();
		// environmentType overrides
		defaults.getValueByEnvironmentType().forEach((environmentType, value) -> {
				boolean active = defaultValueWinner
					.filter(winner -> winner.type == DefaultSettingValueWinnerType.ENVIRONMENT_TYPE)
					.filter(winner -> winner.environmentType.equals(environmentType.getPersistentString()))
					.isPresent();
				dtos.add(new ClusterSettingDefaultJspDto(
						false,
						environmentType.getPersistentString(),
						null,
						null,
						null,
						setting.toStringValue(value),
						active));
				});
		// serverType overrides
		defaults.getValueByServerTypeByEnvironmentType().forEach((environmentType, defaultByServerType) ->
				defaultByServerType.forEach((serverType, value) -> {
					boolean active = defaultValueWinner
							.filter(winner -> winner.type == DefaultSettingValueWinnerType.SERVER_TYPE)
							.filter(winner -> winner.environmentType.equals(environmentType.getPersistentString()))
							.filter(winner -> winner.serverType.equals(serverType))
							.isPresent();
					dtos.add(new ClusterSettingDefaultJspDto(
						false,
						environmentType.getPersistentString(),
						null,
						serverType,
						null,
						setting.toStringValue(value),
						active));
					}));
		// serverName overrides
		defaults.getValueByServerNameByEnvironmentType().forEach((environmentType, defaultByServerName) ->
				defaultByServerName.forEach((serverName, value) -> {
					boolean active = defaultValueWinner
							.filter(winner -> winner.type == DefaultSettingValueWinnerType.SERVER_NAME)
							.filter(winner -> winner.environmentType.equals(environmentType.getPersistentString()))
							.filter(winner -> winner.serverName.equals(serverName))
							.isPresent();
					dtos.add(new ClusterSettingDefaultJspDto(
						false,
						environmentType.getPersistentString(),
						null,
						null,
						serverName,
						setting.toStringValue(value),
						active));
					}));
		// environmentName overrides
		defaults.getValueByEnvironmentNameByEnvironmentType().forEach((environmentType, defaultByEnvironment) ->
				defaultByEnvironment.forEach((environmentName, value) -> {
					boolean active = defaultValueWinner
							.filter(winner -> winner.type == DefaultSettingValueWinnerType.SERVER_NAME)
							.filter(winner -> winner.environmentType.equals(environmentType.getPersistentString()))
							.filter(winner -> winner.environmentName.equals(environmentName))
							.isPresent();
					dtos.add(new ClusterSettingDefaultJspDto(
						false,
						environmentType.getPersistentString(),
						environmentName,
						null,
						null,
						setting.toStringValue(value),
						active));
					}));
		return dtos;
	}

	private <T> List<ClusterSettingTagJspDto> toSettingTagJspDto(DefaultSettingValue<T> defaults,
			Optional<DefaultSettingValueWinner> defaultValueWinner){
		return defaults.getValueBySettingTag().entrySet().stream()
				.map(tag -> {
					boolean active = defaultValueWinner
							.filter(winner -> winner.type == DefaultSettingValueWinnerType.SETTING_TAG)
							.filter(winner -> winner.settingTag.equals(tag.getKey().getPersistentString()))
							.isPresent();
					return new ClusterSettingTagJspDto(
							tag.getKey().getPersistentString(),
							String.valueOf(tag.getValue()),
							active);
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

	public List<ClusterSettingDefaultJspDto> getCodeOverrides(){
		return codeOverrides;
	}

	public List<ClusterSettingTagJspDto> getSettingTags(){
		return settingTags;
	}

}
