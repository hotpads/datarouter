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
package io.datarouter.storage.config.setting.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.constant.ConstantBooleanSetting;

/**
 * It's recommended to use DatarouterClientAvailabilitySettingsFactory to avoid a circular dependency
 */
@Singleton
public class DatarouterClientAvailabilitySettings extends SettingNode{

	public static final String SETTING_PREFIX = "datarouter.availability.";
	public static final String READ = "read";
	public static final String WRITE = "write";

	private final Map<String,AvailabilitySettingNode> availabilityByClientName;
	private final DatarouterClients clients;

	@Inject
	public DatarouterClientAvailabilitySettings(SettingFinder finder, DatarouterClients clients,
			DatarouterClientAvailabilitySwitchThresholdSettings clientAvailabilitySwitchThresholdSettings){
		super(finder, SETTING_PREFIX);
		this.clients = clients;
		availabilityByClientName = new HashMap<>();

		registerChild(clientAvailabilitySwitchThresholdSettings);
	}

	public AvailabilitySettingNode getAvailabilityForClientName(String clientName){
		ClientId clientId = clients.getClientId(clientName);
		if(clientId == null){
			return new AvailabilitySettingNode(this, clientName, false);
		}
		return availabilityByClientName.computeIfAbsent(clientName, name -> new AvailabilitySettingNode(this, name,
				clientId.getDisableable()));
	}

	public static class AvailabilitySettingNode extends SettingNode{

		public final Setting<Boolean> read;
		public final Setting<Boolean> write;

		public AvailabilitySettingNode(DatarouterClientAvailabilitySettings availabilitySettings, String clientName,
				boolean disableable){
			super(availabilitySettings.finder, availabilitySettings.getName() + clientName + ".");

			if(disableable){
				availabilitySettings.registerChild(this);
				read = registerBoolean(READ, true);
				write = registerBoolean(WRITE, true);
			}else{
				read = ConstantBooleanSetting.TRUE;
				write = ConstantBooleanSetting.TRUE;
			}
		}

	}

}
