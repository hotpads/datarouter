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
package io.datarouter.web.config.guice;

import java.util.concurrent.ExecutorService;

import com.google.inject.name.Names;

import io.datarouter.inject.guice.BaseExecutorGuiceModule;

public class DatarouterWebExecutorGuiceModule extends BaseExecutorGuiceModule{

	public static final String
			POOL_lookupCache = "lookupCache";

	private static final ThreadGroup
			datarouterWeb = new ThreadGroup("datarouterWeb");

	@Override
	protected void configure(){
		bind(ExecutorService.class)
				.annotatedWith(Names.named(POOL_lookupCache))
				.toInstance(createLookupCacheExecutor());
	}

	public ExecutorService createLookupCacheExecutor(){
		return createScalingPool(datarouterWeb, POOL_lookupCache, 10);
	}

}
