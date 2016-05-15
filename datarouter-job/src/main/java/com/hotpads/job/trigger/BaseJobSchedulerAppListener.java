package com.hotpads.job.trigger;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import com.hotpads.job.web.TriggersRepository;
import com.hotpads.listener.DatarouterAppListener;

public abstract class BaseJobSchedulerAppListener extends DatarouterAppListener{

	@Inject
	private TriggersRepository triggersRepository;
	@Inject
	private JobScheduler scheduler;

	@Override
	protected final void onStartUp(){
		for(TriggerGroup triggerGroup : getTriggerGroups()){
			triggersRepository.install(triggerGroup);
		}
	}

	@Override
	protected final void onShutDown(){
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

	protected abstract Collection<TriggerGroup> getTriggerGroups();

}
