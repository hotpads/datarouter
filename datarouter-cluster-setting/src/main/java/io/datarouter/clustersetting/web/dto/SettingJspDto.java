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

public class SettingJspDto<T>{

	private final String name;
	private final Boolean hasRedundantCustomValue;
	private final Boolean hasCustomValue;
	private final String value;
	private final String defaultValue;
	private final List<ClusterSettingDefaultJspDto> codeOverrides;

	public SettingJspDto(CachedSetting<T> setting){
		this.name = setting.getName();
		this.hasRedundantCustomValue = setting.getHasRedundantCustomValue();
		this.hasCustomValue = setting.getHasCustomValue();
		this.value = setting.toStringValue();
		this.defaultValue = setting.toStringDefaultValue();
		this.codeOverrides = toDefaults(setting.getDefaultSettingValue());
	}

	private List<ClusterSettingDefaultJspDto> toDefaults(DefaultSettingValue<?> defaults){
		List<ClusterSettingDefaultJspDto> dtos = new ArrayList<>();
		// profile overrides
		defaults.getValueByEnvironmentType().forEach((profile, value) -> {
			String profileString = profile.getPersistentString();
			dtos.add(new ClusterSettingDefaultJspDto(false, profileString, null, null, null, value));
		});
		// serverType overrides
		defaults.getValueByServerTypeByEnvironmentType().forEach((profile, defaultByServerType) -> {
			String profileString = profile.getPersistentString();
			defaultByServerType.forEach((serverType, value) -> dtos.add(new ClusterSettingDefaultJspDto(false,
					profileString, null, serverType, null, value)));
		});
		// serverName overrides
		defaults.getValueByServerNameByEnvironmentType().forEach((profile, defaultByServerName) -> {
			String profileString = profile.getPersistentString();
			defaultByServerName.forEach((serverName, value) -> dtos.add(new ClusterSettingDefaultJspDto(false,
					profileString, null, null, serverName, value)));
		});
		// environment overrides
		defaults.getValueByEnvironmentNameByEnvironmentType().forEach((profile, defaultByEnvironment) -> {
			String profileString = profile.getPersistentString();
			defaultByEnvironment.forEach((environment, value) -> dtos.add(new ClusterSettingDefaultJspDto(false,
					profileString, environment, null, null, value)));
		});
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
