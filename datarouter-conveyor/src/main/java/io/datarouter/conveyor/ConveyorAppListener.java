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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.ConveyorConfigurationGroup.ConveyorPackage;
import io.datarouter.conveyor.config.DatarouterConveyorShouldRunSettings;
import io.datarouter.conveyor.config.DatarouterConveyorThreadCountSettings;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.inject.InstanceRegistry;
import io.datarouter.util.Require;
import io.datarouter.util.concurrent.ExecutorServiceTool;
import io.datarouter.util.concurrent.NamedThreadFactory;
import io.datarouter.web.listener.DatarouterAppListener;

@Singleton
public class ConveyorAppListener implements DatarouterAppListener{
	private static final Logger logger = LoggerFactory.getLogger(ConveyorAppListener.class);

	private static final Map<String,ExecsAndConveyors> execsAndConveyorsByName = new HashMap<>();

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

	@Override
	public final void onStartUp(){
		conveyorConfigurationGroupService.getAllPackages()
				.forEach(this::startConveyor);
	}

	private void startConveyor(ConveyorPackage conveyorPackage){
		String name = conveyorPackage.name();
		Require.notContains(execsAndConveyorsByName.keySet(), name, name + " already exists");
		ConveyorConfiguration configuration = injector.getInstance(conveyorPackage.configurationClass());
		String threadGroupName = name;
		ThreadFactory threadFactory = new NamedThreadFactory(threadGroupName, true);
		int threadCount = threadCountSettings.getSettingForConveyorPackage(conveyorPackage).get();
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(threadCount,
				threadFactory);
		Supplier<Boolean> shouldRun = shouldRunSettings.getSettingForConveyorPackage(conveyorPackage);
		Conveyor conveyor = new Conveyor(conveyorService, configuration, name, shouldRun);
		for(int i = 0; i < threadCount; ++i){
			exec.scheduleWithFixedDelay(conveyor, configuration.delaySeconds(), configuration.delaySeconds(),
					TimeUnit.SECONDS);
		}
		instanceRegistry.register(exec);
		execsAndConveyorsByName.put(name, new ExecsAndConveyors(exec, conveyor));
	}

	@Override
	public final void onShutDown(){
		// explicitly shut those down before CountersAppListener onShutDown
		for(Entry<String,ExecsAndConveyors> entry : execsAndConveyorsByName.entrySet()){
			var conveyor = entry.getValue().conveyor();
			conveyor.setIsShuttingDown();
			if(conveyor.shouldRunOnShutdown()){
				// intentionally run once more to allow cleaner shutdown
				entry.getValue().executor().submit(conveyor);
				logger.info("running conveyor={} onShutdown", entry.getKey());
			}
			ExecutorServiceTool.shutdown(entry.getValue().executor(), Duration.ofSeconds(5));
		}
	}

	public Map<String,ExecsAndConveyors> getExecsAndConveyorsbyName(){
		return execsAndConveyorsByName;
	}

	public record ExecsAndConveyors(
			ExecutorService executor,
			ConveyorRunnable conveyor){
	}

}
