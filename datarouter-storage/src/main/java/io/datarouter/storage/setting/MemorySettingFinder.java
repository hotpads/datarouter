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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.datarouter.scanner.OptionalScanner;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.setting.cached.CachedSetting;

public class MemorySettingFinder implements SettingFinder{

	private final String environmentType;
	private final String environment;
	private final ServerType serverType;
	private final String serverName;
	private final List<DatarouterSettingTag> settingTags;
	// protected so subclasses can modify the settings
	protected final Map<String,Object> settings;

	public MemorySettingFinder(){
		this(null, null, null, null, List.of());
	}

	public MemorySettingFinder(String environmentType, String environment, ServerType serverType, String serverName,
			List<DatarouterSettingTag> settingTags){
		this.environmentType = environmentType;
		this.environment = environment;
		this.serverType = serverType;
		this.serverName = serverName;
		this.settingTags = settingTags;
		this.settings = new ConcurrentHashMap<>();
	}

	@Override
	public String getEnvironmentType(){
		return environmentType;
	}

	@Override
	public String getEnvironmentName(){
		return environment;
	}

	@Override
	public ServerType getServerType(){
		return serverType;
	}

	@Override
	public String getServerName(){
		return serverName;
	}

	@Override
	public List<DatarouterSettingTag> getSettingTags(){
		return settingTags;
	}

	@Override
	public Optional<String> getSettingValue(String name){
		Object value = settings.get(name);
		return Optional.ofNullable(value).map(Object::toString);
	}

	@Override
	public List<String> getAllCustomSettingValues(String name){
		return OptionalScanner.of(getSettingValue(name)).list();
	}

	public void setSettingValue(String name, Object value){
		settings.put(name, value);
	}

	public void clear(){
		settings.clear();
	}

	//settings are already tracked in settings
	@Override
	public void registerCachedSetting(CachedSetting<?> setting){
	}

	//nothing to validate, due to compiler checks
	@Override
	public void validateAllCachedSettings(){
	}

}
