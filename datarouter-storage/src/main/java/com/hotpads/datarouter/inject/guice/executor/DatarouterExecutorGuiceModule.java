package com.hotpads.datarouter.inject.guice.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.google.inject.name.Names;
import com.hotpads.util.core.concurrent.NamedThreadFactory;


public class DatarouterExecutorGuiceModule extends BaseExecutorGuiceModule{

	public static final String 
		POOL_datarouterJobExecutor = "datarouterJobExecutor",
		POOL_countArchiveFlushSchedulerMemory = "countArchiveFlushSchedulerMemory",
		POOL_countArchiveFlushSchedulerDb = "countArchiveFlushSchedulerDb",
		POOL_countArchiveFlusherMemory = "countArchiveFlusherMemory",
		POOL_countArchiveFlusherDb = "countArchiveFlusherDb",
		POOL_writeBehindScheduler = "writeBehindScheduler",
		POOL_writeBehindExecutor = "writeBehindExecutor",
		POOL_datarouterExecutor = "datarouterExecutor",
		POOL_parallelApiCallerFlusher = "parallelApiCallerFlusher",
		POOL_parallelApiCallerSender = "parallelApiCallerSender"
		;
	
	private static final ThreadGroup
		datarouter = new ThreadGroup("datarouter"),
		flushers = new ThreadGroup(datarouter, "flushers")
		;
	
	@Override
	protected void configure(){
		bind(ScheduledExecutorService.class)
			.annotatedWith(Names.named(POOL_datarouterJobExecutor))
			.toInstance(createDatarouterJobExecutor());
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
			.annotatedWith(Names.named(POOL_parallelApiCallerFlusher))
			.toInstance(createParallelApiCallerFlusher());
		bind(ExecutorService.class)
			.annotatedWith(Names.named(POOL_parallelApiCallerSender))
			.toInstance(createParallelApiCallerSender());
		
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

	private ScheduledExecutorService createDatarouterJobExecutor(){
		return createScheduled(datarouter, POOL_datarouterJobExecutor, 10);
	}
	
	private ScheduledExecutorService createWriteBehindScheduler(){
		return createScheduled(datarouter, POOL_writeBehindScheduler, 10);
	}
	
	private ExecutorService createWriteBehindExecutor(){
		return Executors.newCachedThreadPool(new NamedThreadFactory(datarouter, POOL_writeBehindExecutor, true));
	}
	
	private ExecutorService createDatarouterExecutor(){
		return Executors.newCachedThreadPool(new NamedThreadFactory(datarouter, POOL_datarouterExecutor, true));
	}
	
	private ScheduledExecutorService createParallelApiCallerFlusher(){
		return Executors.newScheduledThreadPool(1,
				new NamedThreadFactory(flushers, POOL_parallelApiCallerFlusher, true));
	}
	
	private ExecutorService createParallelApiCallerSender(){
		return Executors.newSingleThreadExecutor(
				new NamedThreadFactory(datarouter, POOL_parallelApiCallerSender, true));
	}
}
