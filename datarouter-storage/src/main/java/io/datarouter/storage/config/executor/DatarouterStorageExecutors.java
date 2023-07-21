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
package io.datarouter.storage.config.executor;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.datarouter.util.concurrent.NamedThreadFactory;
import io.datarouter.util.concurrent.ScalingThreadPoolExecutor;
import jakarta.inject.Singleton;

public class DatarouterStorageExecutors{

	@Singleton
	public static class DatarouterClientFactoryExecutor extends ScalingThreadPoolExecutor{
		public DatarouterClientFactoryExecutor(){
			super("datarouterClientFactoryExecutor", 100);
		}
	}

	@Singleton
	public static class DatarouterSchemaUpdateScheduler extends ScheduledThreadPoolExecutor{
		public DatarouterSchemaUpdateScheduler(){
			super(10, new NamedThreadFactory("schemaUpdateScheduler", true));
		}
	}

}
