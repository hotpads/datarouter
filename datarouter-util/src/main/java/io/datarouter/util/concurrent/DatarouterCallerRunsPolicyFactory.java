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

import java.util.Optional;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterCallerRunsPolicyFactory{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterCallerRunsPolicyFactory.class);

	@Inject
	private DatarouterCallerRunsMonitor datarouterCallerRunsMonitor;

	/**
	 * inspired from java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy.CallerRunsPolicy()
	 * with extra logging
	 */
	public class DatarouterCallerRunsPolicy implements RejectedExecutionHandler{

		@Override
		public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor){
			Optional<String> executorName = NamedThreadFactory.findName(executor.getThreadFactory());
			if(!executor.isShutdown()){
				executorName.ifPresent(datarouterCallerRunsMonitor::inc);
				logger.info("callerRuns executor={}", executorName.orElse(""));
				runnable.run();
			}else{
				logger.info("discarding executor={} runnable={}", executorName.orElse(""), runnable);
			}
		}

	}

}
