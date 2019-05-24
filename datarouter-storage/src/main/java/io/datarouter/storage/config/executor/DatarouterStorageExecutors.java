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
package io.datarouter.storage.config.executor;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.inject.Singleton;

import io.datarouter.util.concurrent.DatarouterExecutorService;
import io.datarouter.util.concurrent.ExecutorTool;
import io.datarouter.util.concurrent.ScalingThreadPoolExecutor;

public class DatarouterStorageExecutors{

	private static final ThreadGroup TG_datarouterStorage = new ThreadGroup("datarouterStorage");
	private static final ThreadGroup TG_flushers = new ThreadGroup(TG_datarouterStorage, "flushers");

	@Singleton
	public static class DatarouterWriteBehindScheduler extends ScheduledThreadPoolExecutor{
		public DatarouterWriteBehindScheduler(){
			super(10, ExecutorTool.createNamedThreadFactory(TG_datarouterStorage, "writeBehindScheduler"));
		}
	}

	@Singleton
	public static class DatarouterWriteBehindExecutor extends ScalingThreadPoolExecutor{
		public DatarouterWriteBehindExecutor(){
			super(TG_datarouterStorage, "writeBehindExecutor", 100);
		}
	}

	@Singleton
	public static class DatarouterClientFactoryExecutor extends DatarouterExecutorService{
		public DatarouterClientFactoryExecutor(){
			super(ExecutorTool.newCachedThreadPool(TG_datarouterStorage, "datarouterClientFactoryExecutor"));
		}
	}

	@Singleton
	public static class DatarouterSchemaUpdateScheduler extends ScheduledThreadPoolExecutor{
		public DatarouterSchemaUpdateScheduler(){
			super(10, ExecutorTool.createNamedThreadFactory(TG_flushers, "schemaUpdateScheduler"));
		}
	}

}
