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
package io.datarouter.conveyor.config;

import java.util.HashMap;
import java.util.Map;

import io.datarouter.conveyor.ConveyorConfigurationGroup.ConveyorPackage;
import io.datarouter.conveyor.ConveyorConfigurationGroupService;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.cached.CachedSetting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterConveyorClusterThreadCountSettings extends SettingNode{

	public static String SETTING_NAME_PREFIX = "clusterThreadCount.";

	private final Map<String,CachedSetting<Integer>> settingByConveyorName = new HashMap<>();

	@Inject
	public DatarouterConveyorClusterThreadCountSettings(
			SettingFinder finder,
			ConveyorConfigurationGroupService conveyorConfigurationGroupService){
		super(finder, DatarouterConveyorSettingRoot.SETTING_NAME_PREFIX + SETTING_NAME_PREFIX);
		conveyorConfigurationGroupService.getAllPackages().stream()
				.map(ConveyorPackage::name)
				.forEach(conveyor -> settingByConveyorName.put(conveyor, registerInteger(conveyor, Integer.MAX_VALUE)));
	}

	public CachedSetting<Integer> getSettingForConveyorPackage(ConveyorPackage conveyorPackage){
		return settingByConveyorName.get(conveyorPackage.name());
	}

}
