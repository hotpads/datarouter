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
package io.datarouter.virtualnode.writebehind.config;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.inject.Singleton;

import io.datarouter.util.concurrent.NamedThreadFactory;
import io.datarouter.util.concurrent.ScalingThreadPoolExecutor;

public class DatarouterVirtualNodeExecutors{

	@Singleton
	public static class DatarouterWriteBehindScheduler extends ScheduledThreadPoolExecutor{
		public DatarouterWriteBehindScheduler(){
			super(10, new NamedThreadFactory("writeBehindScheduler", true));
		}
	}

	@Singleton
	public static class DatarouterWriteBehindExecutor extends ScalingThreadPoolExecutor{
		public DatarouterWriteBehindExecutor(){
			super("writeBehindExecutor", 100);
		}
	}

}
