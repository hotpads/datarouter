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
package io.datarouter.client.hbase.config;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.concurrent.DatarouterCallerRunsPolicyFactory;
import io.datarouter.util.concurrent.DatarouterExecutorService;
import io.datarouter.util.concurrent.NamedThreadFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public class DatarouterHBaseExecutors{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterHBaseExecutors.class);

	@Singleton
	public static class DatarouterHbaseClientExecutor extends DatarouterExecutorService{

		private static final ThreadFactory THREAD_FACTORY = new NamedThreadFactory("hbaseClientExecutor", true);

		@Inject
		public DatarouterHbaseClientExecutor(DatarouterHBaseSettingRoot datarouterHBaseSettingRoot,
				DatarouterCallerRunsPolicyFactory datarouterCallerRunsPolicyFactory){
			super(
					datarouterHBaseSettingRoot.executorThreadCount,
					datarouterHBaseSettingRoot.executorThreadCount,
					1,
					TimeUnit.MINUTES,
					new LinkedBlockingQueue<>(datarouterHBaseSettingRoot.executorQueueSize),
					THREAD_FACTORY,
					datarouterCallerRunsPolicyFactory.new DatarouterCallerRunsPolicy());
			logger.warn("threads={}, queueSize={}",
					datarouterHBaseSettingRoot.executorThreadCount,
					datarouterHBaseSettingRoot.executorQueueSize);
		}

	}

}