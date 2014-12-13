package com.hotpads.job.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.util.core.concurrent.NamedThreadFactory;
import com.hotpads.util.core.concurrent.Provider;

//oops - this was meant to stay in job project
public class JobExecutors{
	static Logger logger = LoggerFactory.getLogger(JobExecutors.class);
	
	public static final ThreadGroup
		job = new ThreadGroup("job"),
		
		dataRouter = new ThreadGroup(job, "dataRouter"),
		jobScheduler = new ThreadGroup(job, "jobScheduler"),
		joblet = new ThreadGroup(job, "joblet"),
		
		flushers = new ThreadGroup(job, "flushers"),
		listingTraits = new ThreadGroup(job, "listingTraits");
		
	public static final ScheduledExecutorService 
		jobExecutor = createScheduled(jobScheduler, "jobExecutor", 10).get();
	
	public static final ExecutorService 
		reputationJobExecutor = new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(10), new ThreadPoolExecutor.CallerRunsPolicy());
	
	public static final ExecutorService 
		adproductsJobExecutor = new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
			new ArrayBlockingQueue<Runnable>(10), new ThreadPoolExecutor.CallerRunsPolicy());

	
	/************************* providers **********************************/
	
	public static final Provider<ScheduledExecutorService>
		traceFlushScheduler = createScheduled(flushers, "traceFlushScheduler", 1),
		
		countArchiveFlushSchedulerMemory = createScheduled(flushers, "countArchiveFlushSchedulerMemory", 1),
		countArchiveFlushSchedulerDb = createScheduled(flushers, "countArchiveFlushSchedulerDb", 1),
		countArchiveFlusherMemory = createScheduled(flushers, "countArchiveFlusherMemory", 1),
		countArchiveFlusherDb = createScheduled(flushers, "countArchiveFlusherDb", 1);
	
	public static final Provider<ExecutorService> listingTrait = createFixedLength(listingTraits,
			"listingTraits", 12);
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
	
	private static Provider<ExecutorService> createFixedLength(final ThreadGroup parentGroup, String name,
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
