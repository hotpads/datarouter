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
import io.datarouter.conveyor.config.DatarouterConveyorSettingRoot;
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

	@Deprecated
	private static final Map<String,ExecAndConveyor> execsAndConveyorsByName = new HashMap<>();
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
		Require.notContains(execsAndConveyorsByName.keySet(), name, name + " already exists");
		ConveyorConfiguration configuration = injector.getInstance(conveyorPackage.configurationClass());

		if(conveyorSettings.enableDynamicThreads.get()){
			var conveyorProcessor = new ConveyorProcessor(
					shouldRunSettings,
					threadCountSettings,
					conveyorSettings,
					conveyorPackage,
					conveyorService,
					configuration,
					instanceRegistry);
			processorByConveyorName.put(name, conveyorProcessor);
		}else{
			ThreadFactory threadFactory = new NamedThreadFactory(name, true);
			int threadCount = threadCountSettings.getSettingForConveyorPackage(conveyorPackage).get();
			Supplier<Boolean> shouldRun = shouldRunSettings.getSettingForConveyorPackage(conveyorPackage);
			ScheduledExecutorService exec = Executors.newScheduledThreadPool(threadCount, threadFactory);
			var conveyor = new Conveyor(conveyorService, configuration, name, shouldRun);
			for(int i = 0; i < threadCount; ++i){
				exec.scheduleWithFixedDelay(
						conveyor,
						configuration.delay().toMillis(),
						configuration.delay().toMillis(),
						TimeUnit.MILLISECONDS);
			}
			instanceRegistry.register(exec);
			var execAndConveyor = new ExecAndConveyor(exec, conveyor, threadCount);
			execsAndConveyorsByName.put(name, execAndConveyor);
		}

	}

	@Override
	public final void onShutDown(){
		// explicitly shut those down before CountersAppListener onShutDown
		for(Entry<String,ExecAndConveyor> entry : execsAndConveyorsByName.entrySet()){
			var conveyor = entry.getValue().conveyor;
			conveyor.setIsShuttingDown();
			if(conveyor.shouldRunOnShutdown()){
				// intentionally run once more to allow cleaner shutdown
				entry.getValue().executor.submit(conveyor);
				logger.info("running conveyor={} onShutdown", entry.getKey());
			}
			if(conveyorSettings.enableDynamicThreads.get()){
				processorByConveyorName.get(entry.getKey()).requestShutdown();
			}else{
				ExecutorServiceTool.shutdown(entry.getValue().executor, Duration.ofSeconds(5));
			}

		}
	}

	public Map<String,ConveyorProcessor> getProcessorByConveyorName(){
		return processorByConveyorName;
	}

	public record ExecAndConveyor(
			ExecutorService executor,
			ConveyorRunnable conveyor,
			int maxNumThreads){
	}

}
