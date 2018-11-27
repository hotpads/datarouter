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

public class DatarouterStorageExecutorGuiceModule extends BaseExecutorGuiceModule{

	// NOTE: these constants' values must exactly match the bean ids in spring
	public static final String POOL_writeBehindScheduler = "writeBehindScheduler";
	public static final String POOL_writeBehindExecutor = "writeBehindExecutor";
	public static final String POOL_datarouterExecutor = "datarouterExecutor";
	public static final String POOL_schemaUpdateScheduler = "schemaUpdateScheduler";

	private static final ThreadGroup THREAD_GROUP_datarouterStorage = new ThreadGroup("datarouterStorage");
	private static final ThreadGroup THREAD_GROUP_flushers = new ThreadGroup(THREAD_GROUP_datarouterStorage,
			"flushers");


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
	}

	//The following factory methods are for Spring
	public ScheduledExecutorService createWriteBehindScheduler(){
		return createScheduled(THREAD_GROUP_datarouterStorage, POOL_writeBehindScheduler, 10);
	}

	public ExecutorService createWriteBehindExecutor(){
		return createScalingPool(THREAD_GROUP_datarouterStorage, POOL_writeBehindExecutor, 100);
	}

	public ExecutorService createDatarouterExecutor(){
		return Executors.newCachedThreadPool(new NamedThreadFactory(THREAD_GROUP_datarouterStorage,
				POOL_datarouterExecutor, true));
	}

	public ScheduledExecutorService createSchemaUpdateScheduler(){
		return Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(THREAD_GROUP_flushers,
				POOL_schemaUpdateScheduler, true));
	}

}
