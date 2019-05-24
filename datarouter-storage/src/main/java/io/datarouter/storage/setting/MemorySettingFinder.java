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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.datarouter.storage.servertype.ServerType;

public class MemorySettingFinder implements SettingFinder{

	private final String configProfile;
	private final ServerType serverType;
	private final String serverName;
	// protected so subclasses can modify the settings
	protected final Map<String,Object> settings;

	public MemorySettingFinder(){
		this(null, null, null);
	}

	public MemorySettingFinder(String configProfile, ServerType serverType, String serverName){
		this.configProfile = configProfile;
		this.serverType = serverType;
		this.serverName = serverName;
		this.settings = new ConcurrentHashMap<>();
	}

	@Override
	public String getConfigProfile(){
		return configProfile;
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
	public Optional<String> getSettingValue(String name){
		Object value = settings.get(name);
		return Optional.ofNullable(value).map(Object::toString);
	}

	public void setSettingValue(String name, Object value){
		settings.put(name, value);
	}

	public void clear(){
		settings.clear();
	}

}
