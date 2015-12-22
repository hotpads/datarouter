package com.hotpads.profile.count.collection.archive;

import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hotpads.guice.DatarouterExecutorGuiceModule;
import com.hotpads.profile.ProfilingSettings;

@Singleton
public class CountArchiveFlusherFactory{

	@Inject
	@Named(DatarouterExecutorGuiceModule.POOL_countArchiveFlushSchedulerDb)
	private ScheduledExecutorService flushSchedulerDb;
	@Inject
	@Named(DatarouterExecutorGuiceModule.POOL_countArchiveFlushSchedulerMemory)
	private ScheduledExecutorService flushSchedulerMemory;
	@Inject
	@Named(DatarouterExecutorGuiceModule.POOL_countArchiveFlusherDb)
	private ScheduledExecutorService flushExecutorDb;
	@Inject
	@Named(DatarouterExecutorGuiceModule.POOL_countArchiveFlusherMemory)
	private ScheduledExecutorService flushExecutorMemory;
	@Inject
	private ProfilingSettings profilingSettings;

	public CountArchiveFlusher createMemoryFlusher(String name, long flushPeriodMs){
		return new CountArchiveFlusher(name, flushPeriodMs, flushSchedulerMemory, flushExecutorMemory,
				profilingSettings);
	}
	
	public CountArchiveFlusher createDbFlusher(String name, long flushPeriodMs){
		return new CountArchiveFlusher(name, flushPeriodMs, flushSchedulerDb, flushExecutorDb, profilingSettings);
	}
	
}
