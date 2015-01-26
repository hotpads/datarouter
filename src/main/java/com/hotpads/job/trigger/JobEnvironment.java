package com.hotpads.job.trigger;

import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hotpads.guice.DatarouterExecutorGuiceModule;
import com.hotpads.job.record.LongRunningTaskTrackerFactory;
import com.hotpads.setting.Setting;

@Singleton
public class JobEnvironment{
	
	private JobScheduler scheduler;
	private ScheduledExecutorService executor;
	private Setting<Boolean> processJobsSetting;
	private LongRunningTaskTrackerFactory longRunningTaskTrackerFactory;
	private String serverName;
	private Setting<Boolean> shouldSaveLongRunningTasks;

	@Inject
	public JobEnvironment(JobScheduler jobScheduler, LongRunningTaskTrackerFactory longRunningTaskTrackerFactory,
			@Named(DatarouterExecutorGuiceModule.POOL_datarouterJobExecutor) ScheduledExecutorService executorService){
		this.scheduler = jobScheduler;
		this.executor = executorService;
		this.longRunningTaskTrackerFactory = longRunningTaskTrackerFactory;
	}

	public final JobScheduler getScheduler(){
		return scheduler;
	}

	public final ScheduledExecutorService getExecutor(){
		return executor;
	}

	public final Setting<Boolean> getProcessJobsSetting(){
		return processJobsSetting;
	}
	
	public final void setProcessJobsSetting(Setting<Boolean> setting) {
		this.processJobsSetting = setting;
	}
	
	public final String getServerName(){
		return serverName;
	}
	
	public final void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	public final LongRunningTaskTrackerFactory getLongRunningTaskTrackerFactory(){
		return longRunningTaskTrackerFactory;
	}

	public Setting<Boolean> getShouldSaveLongRunningTasksSetting() {
		return shouldSaveLongRunningTasks;
	}

	public void setShouldSaveLongRunningTasksSetting(Setting<Boolean> shouldSaveLongRunningTasks) {
		this.shouldSaveLongRunningTasks = shouldSaveLongRunningTasks;
	}
	
	
}
