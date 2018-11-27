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
package io.datarouter.util.concurrent;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorServiceTool{
	private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceTool.class);

	public static void shutdown(ExecutorService exec, Duration timeout){
		String name = "";
		if(exec instanceof ThreadPoolExecutor){
			ThreadPoolExecutor threadPool = (ThreadPoolExecutor)exec;
			ThreadFactory threadFactory = threadPool.getThreadFactory();
			if(threadFactory instanceof NamedThreadFactory){
				name = ((NamedThreadFactory)threadFactory).getGroupName() + "-";
			}
		}
		Duration halfTimeout = timeout.dividedBy(2);
		long halfTimeoutMs = timeout.toMillis();
		logger.info("shutting down {}{}", name, exec);
		exec.shutdown();
		try{
			if(!exec.awaitTermination(halfTimeoutMs, TimeUnit.MILLISECONDS)){
				logger.warn("{}{} did not shut down after {}, interrupting", name, exec, halfTimeout);
				exec.shutdownNow();
				if(!exec.awaitTermination(halfTimeoutMs, TimeUnit.MILLISECONDS)){
					logger.error("could not shut down {}{} after {}", name, exec, timeout);
				}
			}
		}catch(InterruptedException e){
			logger.warn("interrupted while waiting for {}{} to shut down", name, exec);
			exec.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

}
