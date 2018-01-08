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
package io.datarouter.storage.config.guice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.google.inject.name.Names;

import io.datarouter.inject.guice.BaseExecutorGuiceModule;
import io.datarouter.util.concurrent.NamedThreadFactory;


public class DatarouterExecutorGuiceModule extends BaseExecutorGuiceModule{

	//NOTE: these constants' values must exactly match the bean ids in spring
	public static final String
			POOL_writeBehindScheduler = "writeBehindScheduler",
			POOL_writeBehindExecutor = "writeBehindExecutor",
			POOL_datarouterExecutor = "datarouterExecutor",
			POOL_schemaUpdateScheduler = "schemaUpdateScheduler",
			POOL_lookupCache = "lookupCache";

	private static final ThreadGroup
			datarouter = new ThreadGroup("datarouter"),
			flushers = new ThreadGroup(datarouter, "flushers");

	@Override
	protected void configure(){
		bind(ScheduledExecutorService.class)
				.annotatedWith(Names.named(POOL_writeBehindScheduler))
				.toInstance(createWriteBehindScheduler());
		bind(ExecutorService.class)
				.annotatedWith(Names.named(POOL_writeBehindExecutor))
				.toInstance(createWriteBehindExecutor());
		bind(ExecutorService.class)
				.annotatedWith(Names.named(POOL_datarouterExecutor))
				.toInstance(createDatarouterExecutor());
		bind(ScheduledExecutorService.class)
				.annotatedWith(Names.named(POOL_schemaUpdateScheduler))
				.toInstance(createSchemaUpdateScheduler());
		bind(ExecutorService.class)
				.annotatedWith(Names.named(POOL_lookupCache))
				.toInstance(createLookupCacheExecutor());
	}

	//The following factory methods are for Spring
	private ScheduledExecutorService createWriteBehindScheduler(){
		return createScheduled(datarouter, POOL_writeBehindScheduler, 10);
	}

	private ExecutorService createWriteBehindExecutor(){
		return createScalingPool(datarouter, POOL_writeBehindExecutor, 100);
	}

	private ExecutorService createDatarouterExecutor(){
		return Executors.newCachedThreadPool(new NamedThreadFactory(datarouter, POOL_datarouterExecutor, true));
	}

	private ScheduledExecutorService createSchemaUpdateScheduler(){
		return Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(flushers, POOL_schemaUpdateScheduler,
				true));
	}

	private ExecutorService createLookupCacheExecutor(){
		return createScalingPool(datarouter, POOL_lookupCache, 10);
	}

}
