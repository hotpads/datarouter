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

	private final T globalDefault;
	private final Map<DatarouterConfigProfile,T> valueByProfile;
	private final Map<DatarouterConfigProfile,Map<String,T>> valueByServerTypeByProfile;

	public DefaultSettingValue(T globalDefault){
		this.globalDefault = globalDefault;
		this.valueByProfile = new HashMap<>();
		this.valueByServerTypeByProfile = new HashMap<>();
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

	/*--------- convenience ---------------*/

	//Convenience method so callers can optionally save a method call.
	public DefaultSettingValue<T> with(Supplier<DatarouterConfigProfile> profile, T value){
		return with(profile.get(), value);
	}

	public DefaultSettingValue<T> with(Supplier<DatarouterConfigProfile> profile, ServerType serverType, T value){
		return with(profile.get(), serverType, value);
	}

	/*--------- getValue ---------------*/

	public T getValue(DatarouterConfigProfile profile, ServerType serverType){
		Map<String,T> valueByServerType = valueByServerTypeByProfile.get(profile);
		if(valueByServerType != null){
			T value = valueByServerType.get(serverType.getPersistentString());
			if(value != null){
				return value;
			}
		}
		return valueByProfile.getOrDefault(profile, globalDefault);
	}

	public T getValue(String configProfileString, ServerType serverType){
		return getValue(new DatarouterConfigProfile(configProfileString), serverType);
	}

}
