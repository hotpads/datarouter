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
package io.datarouter.conveyor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;

public abstract class ConveyorConfigurationGroup implements PluginConfigValue<ConveyorConfigurationGroup>{

	public static final PluginConfigKey<ConveyorConfigurationGroup> KEY = new PluginConfigKey<>(
			"conveyorConfigurationGroup",
			PluginConfigType.CLASS_LIST);

	private final Map<String,ConveyorPackage> conveyorPackages;

	public ConveyorConfigurationGroup(){
		conveyorPackages = new HashMap<>();
	}

	public void registerConveyor(
			String name,
			Class<? extends ConveyorConfiguration> configurationClass){
		conveyorPackages.put(configurationClass.getSimpleName(), new ConveyorPackage(name, configurationClass));
	}

	public List<ConveyorPackage> getConveyorPackages(){
		return new ArrayList<>(conveyorPackages.values());
	}

	public ConveyorPackage getPackageFromConfigurationClass(Class<? extends ConveyorConfiguration> configurationClass){
		return conveyorPackages.get(configurationClass.getSimpleName());
	}

	@Override
	public PluginConfigKey<ConveyorConfigurationGroup> getKey(){
		return KEY;
	}

	public record ConveyorPackage(
			String name,
			Class<? extends ConveyorConfiguration> configurationClass){
	}

}
