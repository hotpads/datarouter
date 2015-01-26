package com.hotpads.guice;

import java.util.concurrent.ScheduledExecutorService;

import com.google.inject.name.Names;


public class DatarouterExecutorGuiceModule extends BaseExecutorGuiceModule{

	public static final String 
		POOL_datarouterJobExecutor = "datarouterJobExecutor",
		POOL_countArchiveFlushSchedulerMemory = "countArchiveFlushSchedulerMemory",
		POOL_countArchiveFlushSchedulerDb = "countArchiveFlushSchedulerDb",
		POOL_countArchiveFlusherMemory = "countArchiveFlusherMemory",
		POOL_countArchiveFlusherDb = "countArchiveFlusherDb";
	
	private static final ThreadGroup
		datarouter = new ThreadGroup("datarouter"),
		flushers = new ThreadGroup(datarouter, "flushers");
	
	@Override
	protected void configure(){
		bindScheduled(datarouter, POOL_datarouterJobExecutor, 10);
		
		bind(ScheduledExecutorService.class)
			.annotatedWith(Names.named(POOL_countArchiveFlushSchedulerMemory))
			.toInstance(createCountArchiveFlushSchedulerMemory());
		bind(ScheduledExecutorService.class)
			.annotatedWith(Names.named(POOL_countArchiveFlushSchedulerDb))
			.toInstance(createCountArchiveFlushSchedulerDb());
		bind(ScheduledExecutorService.class)
			.annotatedWith(Names.named(POOL_countArchiveFlusherMemory))
			.toInstance(createCountArchiveFlusherMemory());
		bind(ScheduledExecutorService.class)
			.annotatedWith(Names.named(POOL_countArchiveFlusherDb))
			.toInstance(createCountArchiveFlusherDb());
	}
	
	//The following factory methods are for Spring
	private ScheduledExecutorService createCountArchiveFlushSchedulerMemory(){
		return createScheduled(flushers, POOL_countArchiveFlushSchedulerMemory, 1);
	}
	
	private ScheduledExecutorService createCountArchiveFlushSchedulerDb(){
		return createScheduled(flushers, POOL_countArchiveFlushSchedulerDb, 1);
	}
	
	private ScheduledExecutorService createCountArchiveFlusherMemory(){
		return createScheduled(flushers, POOL_countArchiveFlusherMemory, 1);
	}
	
	private ScheduledExecutorService createCountArchiveFlusherDb(){
		return createScheduled(flushers, POOL_countArchiveFlusherDb, 1);
	}

}
