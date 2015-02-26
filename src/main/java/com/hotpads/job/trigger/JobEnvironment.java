package com.hotpads.job.trigger;

import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.guice.DatarouterExecutorGuiceModule;
import com.hotpads.job.record.LongRunningTaskTrackerFactory;
import com.hotpads.setting.Setting;

@Singleton
public class JobEnvironment{
	
	private JobScheduler scheduler;
	private ScheduledExecutorService executor;
	private LongRunningTaskTrackerFactory longRunningTaskTrackerFactory;
	private JobSettings jobSettings;
	private DatarouterContext datarouterContext;

	@Inject
	public JobEnvironment(JobScheduler jobScheduler, LongRunningTaskTrackerFactory longRunningTaskTrackerFactory,
			@Named(DatarouterExecutorGuiceModule.POOL_datarouterJobExecutor) ScheduledExecutorService executorService,
			JobSettings jobSettings, DatarouterContext datarouterContext){
		this.scheduler = jobScheduler;
		this.executor = executorService;
		this.longRunningTaskTrackerFactory = longRunningTaskTrackerFactory;
		this.jobSettings = jobSettings;
		this.datarouterContext = datarouterContext;
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
		return datarouterContext.getServerName();
	}
	
	public final LongRunningTaskTrackerFactory getLongRunningTaskTrackerFactory(){
		return longRunningTaskTrackerFactory;
	}

	public Setting<Boolean> getShouldSaveLongRunningTasksSetting() {
		return jobSettings.getSaveLongRunningTasks();
	}
	
}
