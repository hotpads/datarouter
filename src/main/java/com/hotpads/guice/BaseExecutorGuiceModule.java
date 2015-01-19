package com.hotpads.guice;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.hotpads.util.core.concurrent.NamedThreadFactory;

public abstract class BaseExecutorGuiceModule extends AbstractModule{
	private static final Logger logger = LoggerFactory.getLogger(BaseExecutorGuiceModule.class);
	
	protected void bindThreadPool(ThreadGroup threadGroup, String name, int minThreadCound, int maxThreadCount,
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
	
	protected void bindScheduled(ThreadGroup threadGroup, String name, int threadCount){
		bind(ScheduledExecutorService.class)
			.annotatedWith(Names.named(name))
			.toInstance(createScheduled(threadGroup, name, threadCount));
	}
	
	private ExecutorService createThreadPool(ThreadGroup parentGroup, String name, int minThreadCound,
			int maxThreadCount, int queueSize, RejectedExecutionHandler rejectPolicy){
		NamedThreadFactory namedThreadFactory = new NamedThreadFactory(parentGroup, name, true);
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(queueSize);
		logger.info(name + " initialization " + System.identityHashCode(namedThreadFactory));
		return new ThreadPoolExecutor(minThreadCound, maxThreadCount, 0, TimeUnit.MILLISECONDS, queue, rejectPolicy);
	}
	
	private ExecutorService createFixedPool(ThreadGroup parentGroup, String name, int numThreads){
		NamedThreadFactory namedThreadFactory = new NamedThreadFactory(parentGroup, name, true);
		logger.info(name + " initialization " + System.identityHashCode(namedThreadFactory));
		return Executors.newFixedThreadPool(numThreads, namedThreadFactory);
	}
	
	private ScheduledExecutorService createScheduled(ThreadGroup parentGroup, String name, int numThreads){
		NamedThreadFactory namedThreadFactory = new NamedThreadFactory(parentGroup, name, true);
		logger.info(name + " initialization " + System.identityHashCode(namedThreadFactory));
		return Executors.newScheduledThreadPool(numThreads, namedThreadFactory);
	}
}
