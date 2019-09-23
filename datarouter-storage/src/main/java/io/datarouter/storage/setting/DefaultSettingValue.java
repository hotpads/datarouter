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

import io.datarouter.storage.config.environment.DatarouterEnvironmentType;
import io.datarouter.storage.servertype.ServerType;

public class DefaultSettingValue<T>{

	private T globalDefault;
	private final Map<DatarouterEnvironmentType,T> valueByEnvironmentType;
	private final Map<DatarouterEnvironmentType,Map<String,T>> valueByServerTypeByEnvironmentType;
	private final Map<DatarouterEnvironmentType,Map<String,T>> valueByServerNameByEnvironmentType;
	private final Map<DatarouterEnvironmentType,Map<String,T>> valueByEnvironmentNameByEnvironmentType;

	public DefaultSettingValue(T globalDefault){
		this.globalDefault = globalDefault;
		this.valueByEnvironmentType = new HashMap<>();
		this.valueByServerTypeByEnvironmentType = new HashMap<>();
		this.valueByServerNameByEnvironmentType = new HashMap<>();
		this.valueByEnvironmentNameByEnvironmentType = new HashMap<>();
	}

	/*--------- builder ---------------*/

	public DefaultSettingValue<T> withEnvironmentType(Supplier<DatarouterEnvironmentType> environmentType, T value){
		return withEnvironmentType(environmentType.get(), value);
	}

	public DefaultSettingValue<T> withEnvironmentType(DatarouterEnvironmentType environmentType, T value){
		valueByEnvironmentType.put(environmentType, value);
		return this;
	}

	public DefaultSettingValue<T> withEnvironmentName(Supplier<DatarouterEnvironmentType> environmentType,
		String environment, T value){
		return withEnvironmentName(environmentType.get(), environment, value);
	}

	public DefaultSettingValue<T> withEnvironmentName(DatarouterEnvironmentType environmentType, String environment,
		T value){
		valueByEnvironmentNameByEnvironmentType.putIfAbsent(environmentType, new HashMap<>());
		valueByEnvironmentNameByEnvironmentType.get(environmentType).put(environment, value);
		return this;
	}

	public DefaultSettingValue<T> withServerType(Supplier<DatarouterEnvironmentType> environmentType,
			ServerType serverType, T value){
		return withServerType(environmentType.get(), serverType, value);
	}

	public DefaultSettingValue<T> withServerType(DatarouterEnvironmentType environmentType, ServerType serverType,
			T value){
		valueByServerTypeByEnvironmentType.putIfAbsent(environmentType, new HashMap<>());
		valueByServerTypeByEnvironmentType.get(environmentType).put(serverType.getPersistentString(), value);
		return this;
	}

	public DefaultSettingValue<T> withServerName(Supplier<DatarouterEnvironmentType> environmentType, String serverName,
			T value){
		return withServerName(environmentType.get(), serverName, value);
	}

	public DefaultSettingValue<T> withServerName(DatarouterEnvironmentType environmentType, String serverName, T value){
		valueByServerNameByEnvironmentType.putIfAbsent(environmentType, new HashMap<>());
		valueByServerNameByEnvironmentType.get(environmentType).put(serverName, value);
		return this;
	}

	/*---------- override ---------------*/

	public DefaultSettingValue<T> setGlobalDefault(T value){
		this.globalDefault = value;
		return this;
	}

	/*---------- getValues --------------*/

	public Map<DatarouterEnvironmentType,Map<String,T>> getValueByServerTypeByEnvironmentType(){
		return valueByServerTypeByEnvironmentType;
	}

	public Map<DatarouterEnvironmentType,Map<String,T>> getValueByServerNameByEnvironmentType(){
		return valueByServerNameByEnvironmentType;
	}

	public Map<DatarouterEnvironmentType,Map<String,T>> getValueByEnvironmentNameByEnvironmentType(){
		return valueByEnvironmentNameByEnvironmentType;
	}

	public Map<DatarouterEnvironmentType,T> getValueByEnvironmentType(){
		return valueByEnvironmentType;
	}

	public Map<String,T> getValueByServerType(DatarouterEnvironmentType environmentType){
		return valueByServerTypeByEnvironmentType.get(environmentType);
	}

	public Map<String,T> getValueByServerName(DatarouterEnvironmentType environmentType){
		return valueByServerNameByEnvironmentType.get(environmentType);
	}

	/*--------- getValue ---------------*/

	public T getGlobalDefault(){
		return globalDefault;
	}

	public T getValue(String environmentTypeString, String environmentName, ServerType serverType, String serverName){
		return getValue(new DatarouterEnvironmentType(environmentTypeString), environmentName, serverType, serverName);
	}

	public T getValue(DatarouterEnvironmentType environmentType, String environmentName, ServerType serverType,
			String serverName){
		String serverTypeString = serverType == null ? null : serverType.getPersistentString();
		return getValue(environmentType, environmentName, serverTypeString, serverName);
	}

	public T getValue(DatarouterEnvironmentType environmentType, String environmentName, String serverTypeString,
			String serverName){
		Map<String, T> valueByEnvironmentType = valueByEnvironmentNameByEnvironmentType.get(environmentType);
		if(valueByEnvironmentType != null){
			T value = valueByEnvironmentType.get(environmentName);
			if(value != null){
				return value;
			}
		}
		Map<String,T> valueByServerType = valueByServerTypeByEnvironmentType.get(environmentType);
		if(valueByServerType != null){
			T value = valueByServerType.get(serverTypeString);
			if(value != null){
				return value;
			}
		}
		Map<String,T> valueByServerName = valueByServerNameByEnvironmentType.get(environmentType);
		if(valueByServerName != null){
			T value = valueByServerName.get(serverName);
			if(value != null){
				return value;
			}
		}
		return this.valueByEnvironmentType.getOrDefault(environmentType, globalDefault);
	}

}
