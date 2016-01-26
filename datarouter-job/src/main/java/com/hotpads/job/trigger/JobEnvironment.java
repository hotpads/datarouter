package com.hotpads.job.trigger;

import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hotpads.datarouter.inject.guice.executor.DatarouterExecutorGuiceModule;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.job.record.LongRunningTaskTrackerFactory;

@Singleton
public class JobEnvironment{
	
	private JobScheduler scheduler;
	private ScheduledExecutorService executor;
	private LongRunningTaskTrackerFactory longRunningTaskTrackerFactory;
	private JobSettings jobSettings;
	private Datarouter datarouter;

	@Inject
	public JobEnvironment(JobScheduler jobScheduler, LongRunningTaskTrackerFactory longRunningTaskTrackerFactory,
			@Named(DatarouterExecutorGuiceModule.POOL_datarouterJobExecutor) ScheduledExecutorService executorService,
			JobSettings jobSettings, Datarouter datarouter){
		this.scheduler = jobScheduler;
		this.executor = executorService;
		this.longRunningTaskTrackerFactory = longRunningTaskTrackerFactory;
		this.jobSettings = jobSettings;
		this.datarouter = datarouter;
	}

	public final JobScheduler getScheduler(){
		return scheduler;
	}

	public final ScheduledExecutorService getExecutor(){
		return executor;
	}

	public final Setting<Boolean> getProcessJobsSetting(){
		return jobSettings.getProcessJobs();
	}
	
	public final String getServerName(){
		return datarouter.getServerName();
	}
	
	public final LongRunningTaskTrackerFactory getLongRunningTaskTrackerFactory(){
		return longRunningTaskTrackerFactory;
	}

	public Setting<Boolean> getShouldSaveLongRunningTasksSetting() {
		return jobSettings.getSaveLongRunningTasks();
	}
	
}
