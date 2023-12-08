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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.ConveyorConfigurationGroup.ConveyorPackage;
import io.datarouter.conveyor.config.DatarouterConveyorSettingRoot;
import io.datarouter.conveyor.config.DatarouterConveyorShouldRunSettings;
import io.datarouter.conveyor.config.DatarouterConveyorThreadCountSettings;
import io.datarouter.inject.InstanceRegistry;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.concurrent.ExecutorServiceTool;
import io.datarouter.util.concurrent.NamedThreadFactory;

public class ConveyorProcessor{
	private static final Logger logger = LoggerFactory.getLogger(ConveyorProcessor.class);

	private static final Duration MAX_WAIT_FOR_SHUTDOWN = Duration.ofSeconds(5);
	private static final Duration SLEEP_TIME_WHEN_DISABLED = Duration.ofSeconds(5);

	private final DatarouterConveyorThreadCountSettings threadCountSettings;
	private final DatarouterConveyorShouldRunSettings shouldRunSettings;
	private final DatarouterConveyorSettingRoot conveyorSetting;
	private final ConveyorPackage conveyorPackage;
	private final ConveyorService conveyorService;
	private final ConveyorConfiguration configuration;
	private final ThreadPoolExecutor exec;
	private final CompletionService<?> completionService;
	private final Thread driverThread;
	private final Set<Future<?>> conveyorFutures;

	public ConveyorProcessor(
			DatarouterConveyorShouldRunSettings shouldRunSettings,
			DatarouterConveyorThreadCountSettings threadCountSettings,
			DatarouterConveyorSettingRoot conveyorSetting,
			ConveyorPackage conveyorPackage,
			ConveyorService conveyorService,
			ConveyorConfiguration configuration,
			InstanceRegistry instanceRegistry){
		this.threadCountSettings = threadCountSettings;
		this.shouldRunSettings = shouldRunSettings;
		this.conveyorSetting = conveyorSetting;
		this.conveyorPackage = conveyorPackage;
		this.conveyorService = conveyorService;
		this.configuration = configuration;
		ThreadFactory threadFactory = new NamedThreadFactory("conveyor-" + conveyorPackage.name(), true);
		exec = instanceRegistry.register(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1L, TimeUnit.MINUTES,
				new SynchronousQueue<>(), threadFactory));
		completionService = new ExecutorCompletionService<>(exec);
		driverThread = new Thread(null, this::run, conveyorPackage.name() + " ConveyorProcessor worker thread");
		driverThread.start();
		conveyorFutures = new HashSet<>();
	}

	private void run(){
		Supplier<Integer> numMaxThreads = threadCountSettings.getSettingForConveyorPackage(conveyorPackage);
		submitTasks(numMaxThreads.get());
		while(true){
			try{
				if(Thread.interrupted()){
					logger.warn("conveyor thread shutting down for name=" + conveyorPackage.name());
					return;
				}
				Supplier<Boolean> shouldRun = shouldRunSettings.getSettingForConveyorPackage(conveyorPackage);
				if(!shouldRun.get()){
					sleepABit(SLEEP_TIME_WHEN_DISABLED);
					continue;
				}
				Future<?> completedTask = completionService.poll(conveyorSetting.pollTimeout.get().toMillis(),
						TimeUnit.MILLISECONDS);
				if(completedTask != null){
					// a task has finished, try submitting additional tasks or remove some if necessary
					logger.debug("One task finished, numRunningTasks={}, numAllowedThread={}",
							conveyorFutures.size(),
							numMaxThreads.get());
					conveyorFutures.remove(completedTask);
					sleepABit(conveyorSetting.sleepOnTaskCompletion.get().toJavaDuration());
				}
				submitMoreTasksOrCancelTasks(numMaxThreads);
			}catch(Throwable e){
				logger.error("", e);
			}
		}
	}

	private void submitTasks(int numTasks){
		Supplier<Boolean> shouldRun = shouldRunSettings.getSettingForConveyorPackage(conveyorPackage);
		if(!shouldRun.get()){
			return;
		}
		for(int num = 0; num < numTasks; num++){
			var conveyor = new Conveyor(conveyorService, configuration, conveyorPackage.name(), shouldRun);
			Future<?> conveyorFuture = completionService.submit(conveyor, null);
			conveyorFutures.add(conveyorFuture);
		}
	}

	private void submitMoreTasksOrCancelTasks(Supplier<Integer> numMaxThreads){
		int maxThreads = numMaxThreads.get();
		if(conveyorFutures.size() == maxThreads){
			return;
		}
		if(conveyorFutures.size() < maxThreads){
			logger.debug("Running tasks smaller than allowed threadCounts, {} < {}", conveyorFutures.size(),
					maxThreads);
			submitTasks(maxThreads - conveyorFutures.size());
		}else{
			logger.debug("name={} remove {} tasks because numRunningTasks={} but only numThreads={} are allowed.",
					conveyorPackage.name(),
					conveyorFutures.size() - maxThreads,
					conveyorFutures.size(),
					maxThreads);
			Scanner.of(conveyorFutures)
					.limit(conveyorFutures.size() - maxThreads)
					.forEach(future -> future.cancel(true));
		}
	}

	public void requestShutdown(){
		if(configuration.shouldRunOnShutdown()){
			submitTasks(1);
			logger.info("running conveyor={} onShutdown", conveyorPackage.name());
		}
		driverThread.interrupt();
		ExecutorServiceTool.shutdown(exec, MAX_WAIT_FOR_SHUTDOWN);
	}

	public static void sleepABit(Duration duration){
		try{
			Thread.sleep(duration.toMillis());
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
	}

	public ThreadPoolExecutor getExecutorService(){
		return exec;
	}

	public boolean shouldConveyorRun(){
		return shouldRunSettings.getSettingForConveyorPackage(conveyorPackage).get();
	}

	public int getMaxAllowedThreadCount(){
		return threadCountSettings.getSettingForConveyorPackage(conveyorPackage).get();
	}

}
