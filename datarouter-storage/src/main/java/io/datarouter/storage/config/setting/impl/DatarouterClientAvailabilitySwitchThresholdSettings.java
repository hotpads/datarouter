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

import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;

@Singleton
public class DatarouterClientAvailabilitySwitchThresholdSettings extends SettingNode{

	private final Map<String,Setting<Integer>> switchThresholdByClientName;

	@Inject
	public DatarouterClientAvailabilitySwitchThresholdSettings(SettingFinder finder){
		super(finder, "datarouter.availability.switchThreshold.");

		this.switchThresholdByClientName = new HashMap<>();
	}

	public Setting<Integer> getSwitchThreshold(String clientName){
		return switchThresholdByClientName.computeIfAbsent(clientName, name -> registerInteger(name, 0));
	}

}
