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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.datarouter.conveyor.ConveyorConfigurationGroup.ConveyorPackage;
import io.datarouter.conveyor.config.DatarouterConveyorSettingRoot;
import io.datarouter.conveyor.config.DatarouterConveyorShouldRunSettings;
import io.datarouter.conveyor.config.DatarouterConveyorThreadCountSettings;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.inject.InstanceRegistry;
import io.datarouter.web.listener.DatarouterAppListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ConveyorAppListener implements DatarouterAppListener{

	private static final Map<String,ConveyorProcessor> processorByConveyorName = new HashMap<>();

	@Inject
	private ConveyorConfigurationGroupService conveyorConfigurationGroupService;
	@Inject
	private InstanceRegistry instanceRegistry;
	@Inject
	private DatarouterInjector injector;
	@Inject
	private ConveyorService conveyorService;
	@Inject
	private DatarouterConveyorShouldRunSettings shouldRunSettings;
	@Inject
	private DatarouterConveyorThreadCountSettings threadCountSettings;
	@Inject
	private DatarouterConveyorSettingRoot conveyorSettings;

	@Override
	public final void onStartUp(){
		conveyorConfigurationGroupService.getAllPackages()
				.forEach(this::startConveyor);
	}

	private void startConveyor(ConveyorPackage conveyorPackage){
		String name = conveyorPackage.name();
		ConveyorConfiguration configuration = injector.getInstance(conveyorPackage.configurationClass());

		var conveyorProcessor = new ConveyorProcessor(
				shouldRunSettings,
				threadCountSettings,
				conveyorSettings,
				conveyorPackage,
				conveyorService,
				configuration,
				instanceRegistry);
		processorByConveyorName.put(name, conveyorProcessor);
	}

	@Override
	public final void onShutDown(){
		// explicitly shut those down before CountersAppListener onShutDown
		for(Entry<String,ConveyorProcessor> entry : processorByConveyorName.entrySet()){
			processorByConveyorName.get(entry.getKey()).requestShutdown();

		}
	}

	public Map<String,ConveyorProcessor> getProcessorByConveyorName(){
		return processorByConveyorName;
	}

}
