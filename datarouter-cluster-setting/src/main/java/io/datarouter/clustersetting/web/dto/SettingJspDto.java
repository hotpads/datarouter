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

import io.datarouter.storage.setting.DefaultSettingValue;
import io.datarouter.storage.setting.cached.CachedSetting;

public class SettingJspDto{

	private final String name;
	private final Boolean hasRedundantCustomValue;
	private final Boolean hasCustomValue;
	private final String value;
	private final String defaultValue;
	private final List<ClusterSettingDefaultJspDto> codeOverrides;

	public <T> SettingJspDto(CachedSetting<T> setting){
		this.name = setting.getName();
		this.hasRedundantCustomValue = setting.getHasRedundantCustomValue();
		this.hasCustomValue = setting.getHasCustomValue();
		this.value = setting.toStringValue();
		this.defaultValue = setting.toStringDefaultValue();
		this.codeOverrides = toDefaults(setting, setting.getDefaultSettingValue());
	}

	private <T> List<ClusterSettingDefaultJspDto> toDefaults(CachedSetting<T> setting, DefaultSettingValue<T> defaults){
		List<ClusterSettingDefaultJspDto> dtos = new ArrayList<>();
		// environmentType overrides
		defaults.getValueByEnvironmentType().forEach((environmentType, value) ->
				dtos.add(new ClusterSettingDefaultJspDto(
						false,
						environmentType.getPersistentString(),
						null,
						null,
						null,
						setting.toStringValue(value))));
		// serverType overrides
		defaults.getValueByServerTypeByEnvironmentType().forEach((environmentType, defaultByServerType) ->
				defaultByServerType.forEach((serverType, value) -> dtos.add(new ClusterSettingDefaultJspDto(
						false,
						environmentType.getPersistentString(),
						null,
						serverType,
						null,
						setting.toStringValue(value)))));
		// serverName overrides
		defaults.getValueByServerNameByEnvironmentType().forEach((environmentType, defaultByServerName) ->
				defaultByServerName.forEach((serverName, value) -> dtos.add(new ClusterSettingDefaultJspDto(
						false,
						environmentType.getPersistentString(),
						null,
						null,
						serverName,
						setting.toStringValue(value)))));
		// environmentName overrides
		defaults.getValueByEnvironmentNameByEnvironmentType().forEach((environmentType, defaultByEnvironment) ->
				defaultByEnvironment.forEach((environmentName, value) -> dtos.add(new ClusterSettingDefaultJspDto(
						false,
						environmentType.getPersistentString(),
						environmentName,
						null,
						null,
						setting.toStringValue(value)))));
		return dtos;
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

}
