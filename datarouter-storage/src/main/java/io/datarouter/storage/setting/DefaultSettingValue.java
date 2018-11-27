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
package io.datarouter.storage.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import io.datarouter.storage.config.profile.DatarouterConfigProfile;
import io.datarouter.storage.servertype.ServerType;

public class DefaultSettingValue<T>{

	private T globalDefault;
	private final Map<DatarouterConfigProfile,T> valueByProfile;
	private final Map<DatarouterConfigProfile,Map<String,T>> valueByServerTypeByProfile;
	private final Map<DatarouterConfigProfile,Map<String,T>> valueByServerNameByProfile;

	public DefaultSettingValue(T globalDefault){
		this.globalDefault = globalDefault;
		this.valueByProfile = new HashMap<>();
		this.valueByServerTypeByProfile = new HashMap<>();
		this.valueByServerNameByProfile = new HashMap<>();
	}

	/*--------- builder ---------------*/

	public DefaultSettingValue<T> with(DatarouterConfigProfile profile, T value){
		valueByProfile.put(profile, value);
		return this;
	}

	public DefaultSettingValue<T> with(DatarouterConfigProfile profile, ServerType serverType, T value){
		valueByServerTypeByProfile.putIfAbsent(profile, new HashMap<>());
		valueByServerTypeByProfile.get(profile).put(serverType.getPersistentString(), value);
		return this;
	}

	public DefaultSettingValue<T> with(DatarouterConfigProfile profile, String serverName, T value){
		valueByServerNameByProfile.putIfAbsent(profile, new HashMap<>());
		valueByServerNameByProfile.get(profile).put(serverName, value);
		return this;
	}

	/*--------- convenience ---------------*/

	//Convenience method so callers can optionally save a method call.
	public DefaultSettingValue<T> with(Supplier<DatarouterConfigProfile> profile, T value){
		return with(profile.get(), value);
	}

	public DefaultSettingValue<T> with(Supplier<DatarouterConfigProfile> profile, ServerType serverType, T value){
		return with(profile.get(), serverType, value);
	}

	public DefaultSettingValue<T> with(Supplier<DatarouterConfigProfile> profile, String serverName, T value){
		return with(profile.get(), serverName, value);
	}

	/*---------- override ---------------*/

	public DefaultSettingValue<T> setGlobalDefault(T value){
		this.globalDefault = value;
		return this;
	}

	/*---------- getValues --------------*/

	public Map<DatarouterConfigProfile,Map<String,T>> getValueByServerTypeByProfile(){
		return valueByServerTypeByProfile;
	}

	public Map<DatarouterConfigProfile,Map<String,T>> getValueByServerNameByProfile(){
		return valueByServerNameByProfile;
	}

	public Map<DatarouterConfigProfile,T> getValueByProfile(){
		return valueByProfile;
	}

	public Map<String,T> getValueByServerType(DatarouterConfigProfile configProfile){
		return valueByServerTypeByProfile.get(configProfile);
	}

	public Map<String,T> getValueByServerName(DatarouterConfigProfile configProfile){
		return valueByServerNameByProfile.get(configProfile);
	}

	/*--------- getValue ---------------*/

	public T getGlobalDefault(){
		return globalDefault;
	}

	public T getValue(DatarouterConfigProfile profile, ServerType serverType, String serverName){
		String serverTypeString = serverType == null ? null : serverType.getPersistentString();
		return getValue(profile, serverTypeString, serverName);
	}

	public T getValue(DatarouterConfigProfile profile, String serverTypeString, String serverName){
		Map<String,T> valueByServerType = valueByServerTypeByProfile.get(profile);
		if(valueByServerType != null){
			T value = valueByServerType.get(serverTypeString);
			if(value != null){
				return value;
			}
		}
		Map<String,T> valueByServerName = valueByServerNameByProfile.get(profile);
		if(valueByServerName != null){
			T value = valueByServerName.get(serverName);
			if(value != null){
				return value;
			}
		}
		return valueByProfile.getOrDefault(profile, globalDefault);
	}

	public T getValue(String configProfileString, ServerType serverType, String serverName){
		return getValue(new DatarouterConfigProfile(configProfileString), serverType, serverName);
	}

}
