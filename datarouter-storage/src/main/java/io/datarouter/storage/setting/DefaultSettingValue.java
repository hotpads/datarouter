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
	private final Map<DatarouterEnvironmentType,Map<String,T>> valueByServiceNameByEnvironmentType;
	private final Map<DatarouterEnvironmentType,Map<String,T>> valueByServerNameByEnvironmentType;
	private final Map<DatarouterEnvironmentType,Map<String,T>> valueByEnvironmentNameByEnvironmentType;
	private final Map<DatarouterEnvironmentType,Map<String,T>> valueByEnvironmentCategoryNameByEnvironmentType;
	private final Map<DatarouterSettingTag,Supplier<T>> valueBySettingTag;
	private DefaultSettingValueWinner defaultSettingValueWinner;

	public DefaultSettingValue(T globalDefault){
		this.globalDefault = globalDefault;
		this.valueByEnvironmentType = new HashMap<>();
		this.valueByServerTypeByEnvironmentType = new HashMap<>();
		this.valueByServiceNameByEnvironmentType = new HashMap<>();
		this.valueByServerNameByEnvironmentType = new HashMap<>();
		this.valueByEnvironmentNameByEnvironmentType = new HashMap<>();
		this.valueByEnvironmentCategoryNameByEnvironmentType = new HashMap<>();
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
		valueByEnvironmentNameByEnvironmentType.computeIfAbsent(environmentTypeSupplier.get(), _ -> new HashMap<>())
				.put(environment, value);
		return this;
	}

	public DefaultSettingValue<T> withEnvironmentCategoryName(
			Supplier<DatarouterEnvironmentType> environmentTypeSupplier,
			String categoryName,
			T value){
		valueByEnvironmentCategoryNameByEnvironmentType
				.computeIfAbsent(environmentTypeSupplier.get(), _ -> new HashMap<>())
				.put(categoryName, value);
		return this;
	}

	public DefaultSettingValue<T> withServerType(
			Supplier<DatarouterEnvironmentType> environmentTypeSupplier,
			ServerType serverType,
			T value){
		valueByServerTypeByEnvironmentType.computeIfAbsent(environmentTypeSupplier.get(), _ -> new HashMap<>())
				.put(serverType.getPersistentString(), value);
		return this;
	}

	public DefaultSettingValue<T> withServiceName(
			Supplier<DatarouterEnvironmentType> environmentTypeSupplier,
			String serviceName,
			T value){
		valueByServiceNameByEnvironmentType.computeIfAbsent(environmentTypeSupplier.get(), _ -> new HashMap<>())
				.put(serviceName, value);
		return this;
	}

	public DefaultSettingValue<T> withServerName(
			Supplier<DatarouterEnvironmentType> environmentTypeSupplier,
			String serverName,
			T value){
		valueByServerNameByEnvironmentType.computeIfAbsent(environmentTypeSupplier.get(), _ -> new HashMap<>())
				.put(serverName, value);
		return this;
	}

	public DefaultSettingValue<T> withTag(
			Supplier<DatarouterSettingTag> tagTypeSupplier,
			Supplier<T> value){
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

	public Map<DatarouterEnvironmentType,Map<String,T>> getValueByServiceNameByEnvironmentType(){
		return valueByServiceNameByEnvironmentType;
	}

	public Map<DatarouterEnvironmentType,Map<String,T>> getValueByEnvironmentNameByEnvironmentType(){
		return valueByEnvironmentNameByEnvironmentType;
	}

	public Map<DatarouterEnvironmentType,Map<String,T>> getValueByEnvironmentCategoryNameByEnvironmentType(){
		return valueByEnvironmentCategoryNameByEnvironmentType;
	}

	public Map<DatarouterEnvironmentType,T> getValueByEnvironmentType(){
		return valueByEnvironmentType;
	}

	public boolean hasTags(){
		return !valueBySettingTag.isEmpty();
	}

	public Map<DatarouterSettingTag,Supplier<T>> getValueBySettingTag(){
		return valueBySettingTag;
	}

	public Map<String,T> getValueByServerName(DatarouterEnvironmentType environmentType){
		return valueByServerNameByEnvironmentType.getOrDefault(environmentType, new HashMap<>());
	}

	public Map<String,T> getValueByServerType(DatarouterEnvironmentType environmentType){
		return valueByServerTypeByEnvironmentType.getOrDefault(environmentType, new HashMap<>());
	}

	public Map<String,T> getValueByServiceName(DatarouterEnvironmentType environmentType){
		return valueByServiceNameByEnvironmentType.getOrDefault(environmentType, new HashMap<>());
	}

	public Map<String,T> getValueByEnvironmentName(DatarouterEnvironmentType environmentType){
		return valueByEnvironmentNameByEnvironmentType.getOrDefault(environmentType, new HashMap<>());
	}

	public Map<String,T> getValueByEnvironmentCategoryName(DatarouterEnvironmentType environmentType){
		return valueByEnvironmentCategoryNameByEnvironmentType.getOrDefault(environmentType, new HashMap<>());
	}

	/*--------- getValue ---------------*/

	public T getGlobalDefault(){
		return globalDefault;
	}

	public T getValue(
			String environmentTypeString,
			String environmentCategoryName,
			String environmentName,
			ServerType serverType,
			String serviceName,
			String serverName,
			List<DatarouterSettingTag> settingTags){
		return getValue(
				new DatarouterEnvironmentType(environmentTypeString),
				environmentCategoryName,
				environmentName,
				serverType,
				serviceName,
				serverName,
				settingTags);
	}

	public T getValue(
			DatarouterEnvironmentType environmentType,
			String environmentCategoryName,
			String environmentName,
			ServerType serverType,
			String serviceName,
			String serverName,
			List<DatarouterSettingTag> settingTags){
		String serverTypeString = serverType == null ? null : serverType.getPersistentString();
		return getValue(environmentType, environmentCategoryName, environmentName, serverTypeString,
				serviceName, serverName, settingTags);
	}

	public T getValue(
			DatarouterEnvironmentType environmentType,
			String environmentCategoryName,
			String environmentName,
			String serverTypeString,
			String serviceName,
			String serverName,
			List<DatarouterSettingTag> settingTags){
		Map<String,T> valueByServerName = getValueByServerName(environmentType);
		if(!valueByServerName.isEmpty()){
			T value = valueByServerName.get(serverName);
			if(value != null){
				defaultSettingValueWinner = new DefaultSettingValueWinner(
						DefaultSettingValueWinnerType.SERVER_NAME,
						environmentType.getPersistentString(),
						environmentCategoryName,
						environmentName,
						serverTypeString,
						serviceName,
						serverName,
						String.valueOf(value));
				return value;
			}
		}
		Map<String,T> valueByServiceName = getValueByServiceName(environmentType);
		if(!valueByServiceName.isEmpty()){
			T value = valueByServiceName.get(serviceName);
			if(value != null){
				defaultSettingValueWinner = new DefaultSettingValueWinner(
						DefaultSettingValueWinnerType.SERVICE_NAME,
						environmentType.getPersistentString(),
						environmentCategoryName,
						environmentName,
						serverTypeString,
						serviceName,
						serverName,
						String.valueOf(value));
				return value;
			}
		}
		Map<String,T> valueByServerType = getValueByServerType(environmentType);
		if(!valueByServerType.isEmpty()){
			T value = valueByServerType.get(serverTypeString);
			if(value != null){
				defaultSettingValueWinner = new DefaultSettingValueWinner(
						DefaultSettingValueWinnerType.SERVER_TYPE,
						environmentType.getPersistentString(),
						environmentCategoryName,
						environmentName,
						serverTypeString,
						serviceName,
						serverName,
						String.valueOf(value));
				return value;
			}
		}
		Map<String,T> valueByEnvironmentName = getValueByEnvironmentName(environmentType);
		if(!valueByEnvironmentName.isEmpty()){
			T value = valueByEnvironmentName.get(environmentName);
			if(value != null){
				defaultSettingValueWinner = new DefaultSettingValueWinner(
						DefaultSettingValueWinnerType.ENVIRONMENT_NAME,
						environmentType.getPersistentString(),
						environmentCategoryName,
						environmentName,
						serverTypeString,
						serviceName,
						serverName,
						String.valueOf(value));
				return value;
			}
		}
		Map<String,T> valueByEnvironmentCategoryName = getValueByEnvironmentCategoryName(environmentType);
		if(!valueByEnvironmentCategoryName.isEmpty()){
			T value = valueByEnvironmentCategoryName.get(environmentCategoryName);
			if(value != null){
				defaultSettingValueWinner = new DefaultSettingValueWinner(
						DefaultSettingValueWinnerType.ENVIRONMENT_CATEGORY,
						environmentType.getPersistentString(),
						environmentCategoryName,
						environmentName,
						serverTypeString,
						serviceName,
						serverName,
						String.valueOf(value));
				return value;
			}
		}
		T valueForEnvironmentType = valueByEnvironmentType.get(environmentType);
		if(valueForEnvironmentType != null){
			defaultSettingValueWinner = new DefaultSettingValueWinner(
					DefaultSettingValueWinnerType.ENVIRONMENT_TYPE,
					environmentType.getPersistentString(),
					environmentCategoryName,
					environmentName,
					serverTypeString,
					serviceName,
					serverName,
					String.valueOf(valueForEnvironmentType));
			return valueForEnvironmentType;
		}
		Optional<DatarouterSettingTag> matchedTag = settingTags.stream()
			.filter(valueBySettingTag::containsKey)
			.findFirst();
		if(matchedTag.isPresent()){
			T value = valueBySettingTag.get(matchedTag.get()).get();
			defaultSettingValueWinner = DefaultSettingValueWinner.settingTag(
					matchedTag.get().getPersistentString(),
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
