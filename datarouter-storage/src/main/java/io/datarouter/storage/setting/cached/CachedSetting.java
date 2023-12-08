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
package io.datarouter.storage.setting.cached;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.datarouter.storage.config.environment.DatarouterEnvironmentType;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.setting.DatarouterSettingTag;
import io.datarouter.storage.setting.DefaultSettingValue;
import io.datarouter.storage.setting.DefaultSettingValueWinner;
import io.datarouter.storage.setting.DefaultSettingValueWinner.DefaultSettingValueWinnerType;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingValidator;
import io.datarouter.util.cached.Cached;

//object hierarchy is weird here.  consider using composition
public abstract class CachedSetting<T>
extends Cached<T>
implements Setting<T>{

	protected final SettingFinder finder;
	protected final String name;
	protected final DefaultSettingValue<T> defaultSettingValue;
	protected boolean hasCustomValues; // TODO delete?
	protected boolean hasRedundantCustomValues;
	protected SettingValidator validator;

	public CachedSetting(SettingFinder finder, String name, DefaultSettingValue<T> defaultValue,
			SettingValidator validator){
		super(5, TimeUnit.SECONDS);
		this.finder = finder;
		this.name = name;
		this.defaultSettingValue = defaultValue;
		this.validator = validator;
	}

	@Override
	public boolean isValid(String value){
		return validator.isValid(value);
	}

	/*----------- Object --------------*/

	@Override
	public String toString(){
		return "CachedSetting=" + name;
	}

	/*----------- Setting methods --------------*/

	@Override
	protected T reload(){
		return finder.getSettingValue(name).map(this::parseStringValue).orElseGet(this::getDefaultValue);
	}

	public Optional<T> getMostSpecificDatabeanValue(){
		return finder.getSettingValue(name).map(this::parseStringValue);
	}

	public void validateAllCustomValuesCanBeParsed(){
		try{
			finder.getAllCustomSettingValues(name).forEach(this::parseStringValue);
		}catch(RuntimeException e){
			throw new IllegalArgumentException("value is not valid for setting name=" + name, e);
		}
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public T getDefaultValue(){
		return defaultSettingValue.getValue(
				finder.getEnvironmentType(),
				finder.getEnvironmentCategoryName(),
				finder.getEnvironmentName(),
				finder.getServerType(),
				finder.getServiceName(),
				finder.getServerName(),
				finder.getSettingTags());
	}

	public String toStringValue(T value){
		return String.valueOf(value);
	}

	public String toStringValue(){
		return toStringValue(get());
	}

	public String toStringDefaultValue(){
		return toStringValue(defaultSettingValue.getGlobalDefault());
	}

	@Override
	public boolean getHasCustomValue(){
		return get() != null;
	}

	@Override
	public boolean getHasRedundantCustomValue(){
		return Objects.equals(getDefaultValue(), get());
	}

	/*------------ defaults --------------*/

	public CachedSetting<T> setGlobalDefault(T value){
		defaultSettingValue.setGlobalDefault(value);
		return this;
	}

	public CachedSetting<T> setEnvironmentTypeDefault(Supplier<DatarouterEnvironmentType> environmentType, T value){
		defaultSettingValue.withEnvironmentType(environmentType, value);
		return this;
	}

	public CachedSetting<T> setEnvironmentTypesDefault(
			Collection<Supplier<DatarouterEnvironmentType>> environmentTypes,
			T value){
		environmentTypes.forEach(environmentType -> setEnvironmentTypeDefault(environmentType, value));
		return this;
	}

	public CachedSetting<T> setEnvironmentNameDefault(
			Supplier<DatarouterEnvironmentType> environmentType,
			String environmentName,
			T value){
		defaultSettingValue.withEnvironmentName(environmentType, environmentName, value);
		return this;
	}

	public CachedSetting<T> setEnvironmentCategoryNameDefault(
			Supplier<DatarouterEnvironmentType> environmentType,
			String environmentCategoryName,
			T value){
		defaultSettingValue.withEnvironmentCategoryName(environmentType, environmentCategoryName, value);
		return this;
	}

	public CachedSetting<T> setServerTypeDefault(
			Supplier<DatarouterEnvironmentType> environmentType,
			ServerType serverType,
			T value){
		defaultSettingValue.withServerType(environmentType, serverType, value);
		return this;
	}

	public CachedSetting<T> setServerNameDefault(
			Supplier<DatarouterEnvironmentType> environmentType,
			String serverName,
			T value){
		defaultSettingValue.withServerName(environmentType, serverName, value);
		return this;
	}

	public CachedSetting<T> setServiceNameDefault(
			Supplier<DatarouterEnvironmentType> environmentType,
			String serviceName,
			T value){
		defaultSettingValue.withServiceName(environmentType, serviceName, value);
		return this;
	}

	public CachedSetting<T> setTagDefault(
			Supplier<DatarouterSettingTag> tagTypeSupplier,
			Supplier<T> value){
		defaultSettingValue.withTag(tagTypeSupplier, value);
		return this;
	}

	public DefaultSettingValue<T> getDefaultSettingValue(){
		return defaultSettingValue;
	}

	public DefaultSettingValueWinner getDefaultSettingValueWinner(){
		defaultSettingValue.getValue(
				finder.getEnvironmentType(),
				finder.getEnvironmentCategoryName(),
				finder.getEnvironmentName(),
				finder.getServerType(),
				finder.getServiceName(),
				finder.getServerName(),
				finder.getSettingTags());
		return defaultSettingValue.getDefaultSettingValueWinner();
	}

	public boolean isGlobalDefault(){
		return getMostSpecificDatabeanValue().isEmpty()
				&& getDefaultSettingValueWinner().type == DefaultSettingValueWinnerType.GLOBAL_DEFAULT;
	}

}
