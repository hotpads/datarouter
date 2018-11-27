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
package io.datarouter.storage.setting.cached;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.datarouter.storage.config.profile.DatarouterConfigProfile;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.setting.DefaultSettingValue;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.util.cached.Cached;

//object hierarchy is weird here.  consider using composition
public abstract class CachedSetting<T>
extends Cached<T>
implements Setting<T>{

	protected final SettingFinder finder;
	protected final String name;
	protected final DefaultSettingValue<T> defaultSettingValue;
	protected boolean hasCustomValues;
	protected boolean hasRedundantCustomValues;

	public CachedSetting(SettingFinder finder, String name, DefaultSettingValue<T> defaultValue){
		super(15, TimeUnit.SECONDS);
		this.finder = finder;
		this.name = name;
		this.defaultSettingValue = defaultValue;
	}

	/*----------- Object --------------*/

	@Override
	public String toString(){
		return name;
	}

	/*----------- Setting methods --------------*/

	@Override
	protected T reload(){
		return finder.getSettingValue(name).map(this::parseStringValue).orElse(getDefaultValue());
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public T getDefaultValue(){
		return defaultSettingValue.getValue(finder.getConfigProfile(), finder.getServerType(), finder.getServerName());
	}

	@Override
	public T get(){
		return super.get();
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

	public CachedSetting<T> setProfileDefault(Supplier<DatarouterConfigProfile> profile, T value){
		defaultSettingValue.with(profile, value);
		return this;
	}

	public CachedSetting<T> setProfilesDefault(Collection<Supplier<DatarouterConfigProfile>> profiles, T value){
		profiles.forEach(profile -> setProfileDefault(profile, value));
		return this;
	}

	public CachedSetting<T> setServerTypeDefault(Supplier<DatarouterConfigProfile> profile, ServerType serverType,
			T value){
		defaultSettingValue.with(profile, serverType, value);
		return this;
	}

	public CachedSetting<T> setServerNameDefault(Supplier<DatarouterConfigProfile> profile, String serverName, T value){
		defaultSettingValue.with(profile, serverName, value);
		return this;
	}

	public DefaultSettingValue<T> getDefaultSettingValue(){
		return defaultSettingValue;
	}

}
