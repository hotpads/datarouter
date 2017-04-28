package com.hotpads.job;

import com.hotpads.job.record.LongRunningTaskVacuumJob;
import com.hotpads.job.trigger.BaseTriggerGroup;
import com.hotpads.job.web.BasicJobCategory;
import com.hotpads.job.web.JobCategory;

public class DatarouterJobTriggerGroup extends BaseTriggerGroup{

	private final JobCategory datarouterJob = new BasicJobCategory("Datarouter job", "datarouterJob");

	@Override
	protected void registerTriggers(){
		register(datarouterJob, "0 0/15 * * * ?", LongRunningTaskVacuumJob.class);
	}

}
