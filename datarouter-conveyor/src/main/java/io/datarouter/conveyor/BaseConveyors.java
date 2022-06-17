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
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.inject.InstanceRegistry;
import io.datarouter.util.Require;
import io.datarouter.util.concurrent.ExecutorServiceTool;
import io.datarouter.util.concurrent.NamedThreadFactory;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.listener.DatarouterAppListener;

public abstract class BaseConveyors implements DatarouterAppListener{
	private static final Logger logger = LoggerFactory.getLogger(BaseConveyors.class);

	private static final long DELAY_SEC = 3L;

	private final Map<String,Pair<ExecutorService,Conveyor>> execsAndConveyorsByName;

	@Inject
	private InstanceRegistry instanceRegistry;

	protected BaseConveyors(){
		this.execsAndConveyorsByName = new TreeMap<>();
	}

	protected void start(Conveyor conveyor, int numThreads){
		start(conveyor, numThreads, DELAY_SEC);
	}

	protected void start(Conveyor conveyor, int numThreads, long delaySeconds){
		String name = conveyor.getName();
		Require.notContains(execsAndConveyorsByName.keySet(), name, name + " already exists");
		String threadGroupName = name;
		ThreadFactory threadFactory = new NamedThreadFactory(threadGroupName, true);
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(numThreads, threadFactory);
		for(int i = 0; i < numThreads; ++i){
			exec.scheduleWithFixedDelay(conveyor, delaySeconds, delaySeconds, TimeUnit.SECONDS);
		}
		instanceRegistry.register(exec);
		execsAndConveyorsByName.put(name, new Pair<>(exec, conveyor));
	}

	public Map<String,Pair<ExecutorService,Conveyor>> getExecsAndConveyorsbyName(){
		return execsAndConveyorsByName;
	}

	@Override
	public void onShutDown(){
		// explicitly shut those down before CountersAppListener onShutDown
		for(Entry<String,Pair<ExecutorService,Conveyor>> entry : execsAndConveyorsByName.entrySet()){
			var conveyor = entry.getValue().getRight();
			conveyor.setIsShuttingDown();
			if(conveyor.shouldRunOnShutdown()){
				//intentionally run once more to allow cleaner shutdown
				entry.getValue().getLeft().submit(conveyor);
				logger.info("running conveyor={} onShutdown", entry.getKey());
			}
			ExecutorServiceTool.shutdown(entry.getValue().getLeft(), Duration.ofSeconds(5));
		}
	}

}
