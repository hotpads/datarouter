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
package io.datarouter.storage.setting;

public class DefaultSettingValueWinner{

	public final DefaultSettingValueWinnerType type;
	public final boolean isGlobalDefault;
	public final String environmentType;
	public final String environmentName;
	public final String environmentCategoryName;
	public final String serverType;
	public final String serverName;
	public final String serviceName;
	public final String settingTag;
	public final String value;

	private DefaultSettingValueWinner(
			DefaultSettingValueWinnerType type,
			boolean isGlobalDefault,
			String environmentType,
			String environmentCategoryName,
			String environmentName,
			String serverType,
			String serviceName,
			String serverName,
			String settingTag,
			String value){
		this.type = type;
		this.isGlobalDefault = isGlobalDefault;
		this.environmentType = environmentType;
		this.environmentCategoryName = environmentCategoryName;
		this.environmentName = environmentName;
		this.serverType = serverType;
		this.serviceName = serviceName;
		this.serverName = serverName;
		this.settingTag = settingTag;
		this.value = value;
	}

	public DefaultSettingValueWinner(
			DefaultSettingValueWinnerType type,
			String environmentType,
			String environmentCategoryName,
			String environmentName,
			String serverType,
			String serviceName,
			String serverName,
			String value){
		this(type, false, environmentType, environmentCategoryName, environmentName, serverType, serviceName,
				serverName, null, value);
	}

	public static DefaultSettingValueWinner globalDefault(){
		return new DefaultSettingValueWinner(DefaultSettingValueWinnerType.GLOBAL_DEFAULT, true, null, null, null, null,
				null, null, null, null);
	}

	public static DefaultSettingValueWinner settingTag(String tag, String value){
		return new DefaultSettingValueWinner(DefaultSettingValueWinnerType.SETTING_TAG, false, null, null, null, null,
				null, null, tag, value);
	}

	public enum DefaultSettingValueWinnerType{
		GLOBAL_DEFAULT,
		SERVER_NAME,
		SERVER_TYPE,
		SERVICE_NAME,
		ENVIRONMENT_NAME,
		ENVIRONMENT_TYPE,
		ENVIRONMENT_CATEGORY,
		SETTING_TAG;
	}

}
