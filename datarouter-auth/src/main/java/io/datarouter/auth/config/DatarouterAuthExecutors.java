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
package io.datarouter.auth.config;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.datarouter.util.concurrent.NamedThreadFactory;
import io.datarouter.util.concurrent.ScalingThreadPoolExecutor;
import jakarta.inject.Singleton;

public class DatarouterAuthExecutors{

	@Singleton
	public static class DatarouterAccountCredentialCacheExecutor extends ScheduledThreadPoolExecutor{

		public DatarouterAccountCredentialCacheExecutor(){
			this("DatarouterAccountCredentialCacheExecutor");
		}

		public DatarouterAccountCredentialCacheExecutor(String name){
			super(1, new NamedThreadFactory(name, true));
		}

	}

	@Singleton
	public static class DatarouterAccountDeleteActionExecutor extends ScalingThreadPoolExecutor{

		public DatarouterAccountDeleteActionExecutor(){
			super("datarouterAccountDeleteAction", 10);
		}

	}

}
