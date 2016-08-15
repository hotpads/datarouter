package com.hotpads.job.record;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.setting.Setting;

@Singleton
public class LongRunningTaskTrackerFactory {

	private final DatarouterProperties datarouterProperties;
	private final LongRunningTaskNodeProvider longRunningTaskNodeProvider;

	@Inject
	public LongRunningTaskTrackerFactory(DatarouterProperties datarouterProperties,
			LongRunningTaskNodeProvider longRunningTaskNodeProvider){
		this.datarouterProperties = datarouterProperties;
		this.longRunningTaskNodeProvider = longRunningTaskNodeProvider;
	}


	public LongRunningTaskTracker create(String jobClass, Setting<Boolean> shouldSaveLongRunningTasks,
			LongRunningTaskType type, String triggeredBy){
		LongRunningTask task = new LongRunningTask(jobClass, datarouterProperties.getServerName(), type, triggeredBy);
		return new LongRunningTaskTracker(longRunningTaskNodeProvider.get(), task, shouldSaveLongRunningTasks);
	}

}
