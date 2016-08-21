package com.hotpads.job.record;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.setting.constant.ConstantBooleanSetting;
import com.hotpads.job.trigger.JobSettings;

@Singleton
public class LongRunningTaskTrackerFactory {

	private final DatarouterProperties datarouterProperties;
	private final LongRunningTaskNodeProvider longRunningTaskNodeProvider;
	private final JobSettings jobSettings;

	@Inject
	public LongRunningTaskTrackerFactory(DatarouterProperties datarouterProperties,
			LongRunningTaskNodeProvider longRunningTaskNodeProvider, JobSettings jobSettings){
		this.datarouterProperties = datarouterProperties;
		this.longRunningTaskNodeProvider = longRunningTaskNodeProvider;
		this.jobSettings = jobSettings;
	}


	public LongRunningTaskTracker create(String jobClass, LongRunningTaskType type, String triggeredBy){
		LongRunningTask task = new LongRunningTask(jobClass, datarouterProperties.getServerName(), type, triggeredBy);
		return new LongRunningTaskTracker(longRunningTaskNodeProvider.get(), task, jobSettings
				.getSaveLongRunningTasks());
	}

	//TODO do we need the factory for this, or could we have a NoOpLongRunningTaskTracker implementation?
	public LongRunningTaskTracker createNoOpTracker(String jobClass){
		LongRunningTask task = new LongRunningTask(jobClass, datarouterProperties.getServerName(),
				LongRunningTaskType.NOOP, null);
		return new LongRunningTaskTracker(longRunningTaskNodeProvider.get(), task, new ConstantBooleanSetting(false));
	}

}
