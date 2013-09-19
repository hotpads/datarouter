package com.hotpads.job.setting.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;

import com.hotpads.util.core.concurrent.NamedThreadFactory;
import com.hotpads.util.core.concurrent.Provider;

public class JobExecutors{
	static Logger logger = Logger.getLogger(JobExecutors.class);
	
	public static final ThreadGroup
		job = new ThreadGroup("job"),
		
		
		
		dataRouter = new ThreadGroup(job, "dataRouter"),
		jobScheduler = new ThreadGroup(job, "jobScheduler"),
		joblet = new ThreadGroup(job, "joblet"),
		
		flushers = new ThreadGroup(job, "flushers");
	
	public static final ScheduledExecutorService 
		jobExecutor = createScheduled(jobScheduler, "jobExecutor", 10).get();

	
	/************************* providers **********************************/
	
	public static final Provider<ScheduledExecutorService>
		traceFlushScheduler = createScheduled(flushers, "traceFlushScheduler", 1),
		
		countArchiveFlushSchedulerMemory = createScheduled(flushers, "countArchiveFlushSchedulerMemory", 1),
		countArchiveFlushSchedulerDb = createScheduled(flushers, "countArchiveFlushSchedulerDb", 1),
		countArchiveFlusherMemory = createScheduled(flushers, "countArchiveFlusherMemory", 1),
		countArchiveFlusherDb = createScheduled(flushers, "countArchiveFlusherDb", 1);
	
	
	/**************************** convenience **********************************/
	
	public static Provider<ScheduledExecutorService> createScheduled(final ThreadGroup parentGroup, final String name,
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
}
