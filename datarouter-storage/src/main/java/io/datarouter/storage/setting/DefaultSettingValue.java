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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.storage.config.environment.DatarouterEnvironmentType;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.setting.DefaultSettingValueWinner.DefaultSettingValueWinnerType;

public class DefaultSettingValue<T>{

	private T globalDefault;
	private final Map<DatarouterEnvironmentType,T> valueByEnvironmentType;
	private final Map<DatarouterEnvironmentType,Map<String,T>> valueByServerTypeByEnvironmentType;
	private final Map<DatarouterEnvironmentType,Map<String,T>> valueByServerNameByEnvironmentType;
	private final Map<DatarouterEnvironmentType,Map<String,T>> valueByEnvironmentNameByEnvironmentType;
	private final Map<DatarouterSettingTag,T> valueBySettingTag;
	private DefaultSettingValueWinner defaultSettingValueWinner;

	public DefaultSettingValue(T globalDefault){
		this.globalDefault = globalDefault;
		this.valueByEnvironmentType = new HashMap<>();
		this.valueByServerTypeByEnvironmentType = new HashMap<>();
		this.valueByServerNameByEnvironmentType = new HashMap<>();
		this.valueByEnvironmentNameByEnvironmentType = new HashMap<>();
		this.valueBySettingTag = new HashMap<>();
		this.defaultSettingValueWinner = DefaultSettingValueWinner.globalDefault();
	}

	/*--------- builder ---------------*/

	public DefaultSettingValue<T> withEnvironmentType(
			Supplier<DatarouterEnvironmentType> environmentTypeSupplier,
			T value){
		valueByEnvironmentType.put(environmentTypeSupplier.get(), value);
		return this;
	}

	public DefaultSettingValue<T> withEnvironmentName(
			Supplier<DatarouterEnvironmentType> environmentTypeSupplier,
			String environment,
			T value){
		valueByEnvironmentNameByEnvironmentType.computeIfAbsent(environmentTypeSupplier.get(), $ -> new HashMap<>())
				.put(environment, value);
		return this;
	}

	public DefaultSettingValue<T> withServerType(
			Supplier<DatarouterEnvironmentType> environmentTypeSupplier,
			ServerType serverType,
			T value){
		valueByServerTypeByEnvironmentType.computeIfAbsent(environmentTypeSupplier.get(), $ -> new HashMap<>())
				.put(serverType.getPersistentString(), value);
		return this;
	}

	public DefaultSettingValue<T> withServerName(
			Supplier<DatarouterEnvironmentType> environmentTypeSupplier,
			String serverName,
			T value){
		valueByServerNameByEnvironmentType.computeIfAbsent(environmentTypeSupplier.get(), $ -> new HashMap<>())
				.put(serverName, value);
		return this;
	}

	public DefaultSettingValue<T> withTag(
			Supplier<DatarouterSettingTag> tagTypeSupplier,
			T value){
		valueBySettingTag.put(tagTypeSupplier.get(), value);
		return this;
	}

	/*---------- override ---------------*/

	public DefaultSettingValue<T> setGlobalDefault(T value){
		globalDefault = value;
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

	public Map<DatarouterSettingTag,T> getValueBySettingTag(){
		return valueBySettingTag;
	}

	public Map<String,T> getValueByServerName(DatarouterEnvironmentType environmentType){
		return valueByServerNameByEnvironmentType.getOrDefault(environmentType, new HashMap<>());
	}

	public Map<String,T> getValueByServerType(DatarouterEnvironmentType environmentType){
		return valueByServerTypeByEnvironmentType.getOrDefault(environmentType, new HashMap<>());
	}

	public Map<String,T> getValueByEnvironmentName(DatarouterEnvironmentType environmentType){
		return valueByEnvironmentNameByEnvironmentType.getOrDefault(environmentType, new HashMap<>());
	}

	/*--------- getValue ---------------*/

	public T getGlobalDefault(){
		return globalDefault;
	}

	public T getValue(String environmentTypeString, String environmentName, ServerType serverType, String serverName,
			List<DatarouterSettingTag> settingTags){
		return getValue(new DatarouterEnvironmentType(environmentTypeString), environmentName, serverType, serverName,
				settingTags);
	}

	public T getValue(DatarouterEnvironmentType environmentType, String environmentName, ServerType serverType,
			String serverName, List<DatarouterSettingTag> settingTags){
		String serverTypeString = serverType == null ? null : serverType.getPersistentString();
		return getValue(environmentType, environmentName, serverTypeString, serverName, settingTags);
	}

	public T getValue(DatarouterEnvironmentType environmentType, String environmentName, String serverTypeString,
			String serverName, List<DatarouterSettingTag> settingTags){
		Map<String,T> valueByServerName = getValueByServerName(environmentType);
		if(!valueByServerName.isEmpty()){
			T value = valueByServerName.get(serverName);
			if(value != null){
				defaultSettingValueWinner = new DefaultSettingValueWinner(DefaultSettingValueWinnerType.SERVER_NAME,
						environmentType.getPersistentString(), null, null, serverName, String.valueOf(value));
				return value;
			}
		}
		Map<String,T> valueByServerType = getValueByServerType(environmentType);
		if(!valueByServerType.isEmpty()){
			T value = valueByServerType.get(serverTypeString);
			if(value != null){
				defaultSettingValueWinner = new DefaultSettingValueWinner(DefaultSettingValueWinnerType.SERVER_TYPE,
						environmentType.getPersistentString(), null, serverTypeString, null, String.valueOf(value));
				return value;
			}
		}
		Map<String,T> valueByEnvironmentName = getValueByEnvironmentName(environmentType);
		if(!valueByEnvironmentName.isEmpty()){
			T value = valueByEnvironmentName.get(environmentName);
			if(value != null){
				defaultSettingValueWinner = new DefaultSettingValueWinner(
						DefaultSettingValueWinnerType.ENVIRONMENT_NAME, environmentType.getPersistentString(),
						environmentName, null, null, String.valueOf(value));
				return value;
			}
		}
		T valueForEnvironmentType = valueByEnvironmentType.get(environmentType);
        if(valueForEnvironmentType != null){
			defaultSettingValueWinner = new DefaultSettingValueWinner(DefaultSettingValueWinnerType.ENVIRONMENT_TYPE,
					environmentType.getPersistentString(), null, null, null, String.valueOf(valueForEnvironmentType));
			return valueForEnvironmentType;
		}
		Optional<DatarouterSettingTag> matchedTag = settingTags.stream()
			.filter(valueBySettingTag::containsKey)
			.findFirst();
		if(matchedTag.isPresent()){
			T value = valueBySettingTag.get(matchedTag.get());
			defaultSettingValueWinner = DefaultSettingValueWinner.settingTag(matchedTag.get().getPersistentString(),
					String.valueOf(value));
			return value;
		}
		defaultSettingValueWinner = DefaultSettingValueWinner.globalDefault();
		return globalDefault;
	}

	public DefaultSettingValueWinner getDefaultSettingValueWinner(){
		return defaultSettingValueWinner;
	}

}
