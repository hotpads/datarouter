package com.hotpads.job.trigger;

import java.util.Map;

import javax.inject.Inject;

import com.hotpads.listener.DatarouterAppListener;

public class JobSchedulerAppListener extends DatarouterAppListener{

	@Inject
	private JobScheduler scheduler;
	
	@Override
	protected void onStartUp(){
		scheduler.scheduleJavaTriggers();
	}

	@Override
	protected void onShutDown(){
		TriggerTracker tracker = scheduler.getTracker();
		Map<Class<? extends Job>,TriggerInfo> jobMap = tracker.getMap();
		scheduler.shutDownNow();
		for(Map.Entry<Class<? extends Job>,TriggerInfo> entry : jobMap.entrySet()){
			if(entry.getValue().isRunning()){
				entry.getValue().getJob().getLongRunningTaskTracker().requestStop();
				entry.getValue().setRunning(false);
			}
		}
	}

}
