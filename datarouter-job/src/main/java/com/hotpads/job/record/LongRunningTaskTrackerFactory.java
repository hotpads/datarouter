package com.hotpads.job.record;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.setting.constant.ConstantBooleanSetting;
import com.hotpads.job.trigger.JobSettings;

@Singleton
public class LongRunningTaskTrackerFactory{

	private final DatarouterProperties datarouterProperties;
	private final DatarouterJobRouter datarouterJobRouter;
	private final JobSettings jobSettings;

	@Inject
	public LongRunningTaskTrackerFactory(DatarouterProperties datarouterProperties,
			DatarouterJobRouter datarouterJobRouter, JobSettings jobSettings){
		this.datarouterProperties = datarouterProperties;
		this.datarouterJobRouter = datarouterJobRouter;
		this.jobSettings = jobSettings;
	}


	public LongRunningTaskTracker create(String jobClass, LongRunningTaskType type, String triggeredBy){
		LongRunningTask task = new LongRunningTask(jobClass, datarouterProperties.getServerName(), type, triggeredBy);
		return new LongRunningTaskTracker(datarouterJobRouter.longRunningTask, task, jobSettings
				.getSaveLongRunningTasks());
	}

	//TODO do we need the factory for this, or could we have a NoOpLongRunningTaskTracker implementation?
	public LongRunningTaskTracker createNoOpTracker(String jobClass){
		LongRunningTask task = new LongRunningTask(jobClass, datarouterProperties.getServerName(),
				LongRunningTaskType.NOOP, null);
		return new LongRunningTaskTracker(datarouterJobRouter.longRunningTask, task, ConstantBooleanSetting.FALSE);
	}

}
