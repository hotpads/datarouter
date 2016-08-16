package com.hotpads.job.record;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.constant.ConstantBooleanSetting;

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

	//TODO do we need the factory for this, or could we have a NoOpLongRunningTaskTracker implementation?
	public LongRunningTaskTracker createNoOpTracker(String jobClass){
		LongRunningTask task = new LongRunningTask(jobClass, datarouterProperties.getServerName(),
				LongRunningTaskType.NOOP, null);
		return new LongRunningTaskTracker(longRunningTaskNodeProvider.get(), task, new ConstantBooleanSetting(false));
	}

}
