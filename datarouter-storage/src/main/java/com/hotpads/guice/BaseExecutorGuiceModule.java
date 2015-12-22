package com.hotpads.guice;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.hotpads.util.core.concurrent.NamedThreadFactory;
import com.hotpads.util.core.concurrent.ScalingThreadPoolExecutor;

public abstract class BaseExecutorGuiceModule extends AbstractModule{
	private static final Logger logger = LoggerFactory.getLogger(BaseExecutorGuiceModule.class);

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
		NamedThreadFactory namedThreadFactory = new NamedThreadFactory(parentGroup, name, true);
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueSize);
		logger.info(name + " initialization " + System.identityHashCode(namedThreadFactory));
		return new ThreadPoolExecutor(minThreadCound, maxThreadCount, 0, TimeUnit.MILLISECONDS, queue,
				namedThreadFactory, rejectPolicy);
	}

	protected ExecutorService createFixedPool(ThreadGroup parentGroup, String name, int numThreads){
		NamedThreadFactory namedThreadFactory = new NamedThreadFactory(parentGroup, name, true);
		logger.info(name + " initialization " + System.identityHashCode(namedThreadFactory));
		return Executors.newFixedThreadPool(numThreads, namedThreadFactory);
	}

	protected ScalingThreadPoolExecutor createScalingPool(ThreadGroup parentGroup, String name, int maxThreadCount){
		ThreadFactory threadFactory = new NamedThreadFactory(parentGroup, name, true);
		logger.info(name + " initialization " + System.identityHashCode(threadFactory));
		return new ScalingThreadPoolExecutor(0, maxThreadCount, 1, TimeUnit.SECONDS, threadFactory);
	}

	protected ScheduledExecutorService createScheduled(ThreadGroup parentGroup, String name, int numThreads){
		NamedThreadFactory namedThreadFactory = new NamedThreadFactory(parentGroup, name, true);
		logger.info(name + " initialization " + System.identityHashCode(namedThreadFactory));
		return Executors.newScheduledThreadPool(numThreads, namedThreadFactory);
	}

	private ExecutorService createCached(ThreadGroup threadGroup, String poolName){
		NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, poolName, true);
		return Executors.newCachedThreadPool(threadFactory);
	}

}
