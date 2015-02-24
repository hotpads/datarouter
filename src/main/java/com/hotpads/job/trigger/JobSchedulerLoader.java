package com.hotpads.job.trigger;

import javax.inject.Inject;

import com.hotpads.HotPadsWebAppListener;

public class JobSchedulerLoader extends HotPadsWebAppListener{

	@Inject
	private JobScheduler scheduler;
	
	@Override
	protected void onStartUp(){
		scheduler.scheduleJavaTriggers();
	}

	@Override
	protected void onShutDown(){
		scheduler.shutDownNow();
	}

}
