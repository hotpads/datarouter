package com.hotpads.job.trigger;

import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.job.thread.JobExecutorProvider.JobExecutor;
import com.hotpads.setting.Setting;

@Singleton
public class JobEnvironment{
	
	private JobScheduler scheduler;
	private ScheduledExecutorService executor;
	private Setting<Boolean> processJobsSetting;

	@Inject
	public JobEnvironment(JobScheduler jobScheduler, @JobExecutor ScheduledExecutorService executorService){
		this.scheduler = jobScheduler;
		this.executor = executorService;
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
	
}
