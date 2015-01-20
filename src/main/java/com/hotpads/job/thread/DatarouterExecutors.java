package com.hotpads.job.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.util.core.concurrent.NamedThreadFactory;
import com.hotpads.util.core.concurrent.Provider;

//oops - this was meant to stay in job project
public class DatarouterExecutors{
	static Logger logger = LoggerFactory.getLogger(DatarouterExecutors.class);
	
	private static final ThreadGroup
		datarouter = new ThreadGroup("datarouter"),
		datarouterJobScheduler = new ThreadGroup(datarouter, "datarouterJobScheduler");
		
	public static final ScheduledExecutorService 
		datarouterJobExecutor = createScheduled(datarouterJobScheduler, "datarouterJobExecutor", 10).get();
	
	/**************************** convenience **********************************/
	
	private static Provider<ScheduledExecutorService> createScheduled(final ThreadGroup parentGroup, final String name,
			final int numThreads){
		return new Provider<ScheduledExecutorService>(name){
			@Override protected ScheduledExecutorService initialize(){
				NamedThreadFactory namedThreadFactory = new NamedThreadFactory(parentGroup, name, true);
				logger.warn(name+" initialization "+System.identityHashCode(namedThreadFactory));
//				return Executors.newSingleThreadScheduledExecutor(namedThreadFactory);
				return Executors.newScheduledThreadPool(numThreads, namedThreadFactory);
			}
		};
	}
	
	public static Provider<ExecutorService> createFixedLength(final ThreadGroup parentGroup, String name,
			final int numThreads){
		return new Provider<ExecutorService>(name){
			@Override
			protected ExecutorService initialize(){
				NamedThreadFactory namedThreadFactory = new NamedThreadFactory(parentGroup, name, true);
				logger.info(name + " initialization " + System.identityHashCode(namedThreadFactory));
				return Executors.newFixedThreadPool(numThreads, namedThreadFactory);
			}
		};
	}
}
