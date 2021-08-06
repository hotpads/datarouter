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
package io.datarouter.util.concurrent;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorServiceTool{
	private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceTool.class);

	public static void shutdown(ExecutorService exec, Duration timeout){
		shutdown(exec, timeout, true);
	}

	public static void shutdown(ExecutorService exec, Duration timeout, boolean verboseLogging){
		String name = "";
		if(exec instanceof ThreadPoolExecutor){
			ThreadPoolExecutor threadPool = (ThreadPoolExecutor)exec;
			Optional<String> factoryName = NamedThreadFactory.findName(threadPool.getThreadFactory());
			if(factoryName.isPresent()){
				name = factoryName.get() + "-";
			}
		}
		Duration halfTimeout = timeout.dividedBy(2);
		long halfTimeoutMs = timeout.toMillis();
		if(verboseLogging){
			logger.warn("shutting down name={} {}", name, exec);
		}
		exec.shutdown();
		try{
			if(!exec.awaitTermination(halfTimeoutMs, TimeUnit.MILLISECONDS)){
				logger.warn("not yet terminated, interrupting name={} halfTimeout={} {}", name, halfTimeout, exec);
				List<Runnable> neverCommencedTasks = exec.shutdownNow();
				if(!neverCommencedTasks.isEmpty()){
					logger.error("lost tasks count={} name={} {}", neverCommencedTasks.size(), name, exec);
				}
				if(!exec.awaitTermination(halfTimeoutMs, TimeUnit.MILLISECONDS)){
					logger.error("not terminated name={} timeout={} {}", name, timeout, exec);
				}else{
					logger.warn("executor shutdown after interupt name={} {}", name, exec);
				}
			}else{
				if(verboseLogging){
					logger.warn("executor shutdown cleanly name={} {}", name, exec);
				}
			}
		}catch(InterruptedException e){
			logger.warn("interrupted while waiting for shutdown name={} {}", name, exec);
			exec.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

}
