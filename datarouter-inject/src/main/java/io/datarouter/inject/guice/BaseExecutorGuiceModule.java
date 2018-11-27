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
package io.datarouter.inject.guice;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import io.datarouter.util.concurrent.NamedThreadFactory;
import io.datarouter.util.concurrent.ScalingThreadPoolExecutor;

public abstract class BaseExecutorGuiceModule extends AbstractModule{

	protected void bindPool(ThreadGroup threadGroup, String name, int minThreadCound, int maxThreadCount,
			int queueSize, RejectedExecutionHandler rejectPolicy){
		bind(ExecutorService.class)
			.annotatedWith(Names.named(name))
			.toInstance(createThreadPool(threadGroup, name, minThreadCound, maxThreadCount, queueSize, rejectPolicy));
	}

	protected void bindFixedPool(ThreadGroup threadGroup, String name, int threadCount){
		bind(ExecutorService.class)
			.annotatedWith(Names.named(name))
			.toInstance(createFixedPool(threadGroup, name, threadCount));
	}

	protected void bindScalingPool(ThreadGroup threadGroup, String name, int threadCount){
		bind(ExecutorService.class)
			.annotatedWith(Names.named(name))
			.toInstance(createScalingPool(threadGroup, name, threadCount));
	}

	protected void bindCached(ThreadGroup threadGroup, String name){
		bind(ExecutorService.class)
			.annotatedWith(Names.named(name))
			.toInstance(createCached(threadGroup, name));
	}

	protected void bindScheduled(ThreadGroup threadGroup, String name, int threadCount){
		bind(ScheduledExecutorService.class)
			.annotatedWith(Names.named(name))
			.toInstance(createScheduled(threadGroup, name, threadCount));
	}

	protected ThreadPoolExecutor createThreadPool(ThreadGroup parentGroup, String name, int minThreadCound,
			int maxThreadCount, int queueSize, RejectedExecutionHandler rejectPolicy){
		ThreadFactory threadFactory = new NamedThreadFactory(parentGroup, name, true);
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueSize);
		return new ThreadPoolExecutor(minThreadCound, maxThreadCount, 1, TimeUnit.MINUTES, queue, threadFactory,
				rejectPolicy);
	}

	protected ExecutorService createFixedPool(ThreadGroup parentGroup, String name, int numThreads){
		ThreadFactory threadFactory = new NamedThreadFactory(parentGroup, name, true);
		return Executors.newFixedThreadPool(numThreads, threadFactory);
	}

	protected ScalingThreadPoolExecutor createScalingPool(ThreadGroup parentGroup, String name, int maxThreadCount){
		ThreadFactory threadFactory = new NamedThreadFactory(parentGroup, name, true);
		return new ScalingThreadPoolExecutor(0, maxThreadCount, 1, TimeUnit.MINUTES, threadFactory);
	}

	protected ExecutorService createCached(ThreadGroup threadGroup, String name){
		ThreadFactory threadFactory = new NamedThreadFactory(threadGroup, name, true);
		return Executors.newCachedThreadPool(threadFactory);
	}

	protected ScheduledExecutorService createScheduled(ThreadGroup parentGroup, String name, int numThreads){
		ThreadFactory threadFactory = new NamedThreadFactory(parentGroup, name, true);
		return Executors.newScheduledThreadPool(numThreads, threadFactory);
	}

}
