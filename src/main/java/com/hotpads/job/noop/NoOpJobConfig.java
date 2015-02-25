package com.hotpads.job.noop;

import com.hotpads.job.record.LongRunningTaskDao;
import com.hotpads.job.trigger.JobConfigModule;
import com.hotpads.job.trigger.TriggerGroup;
import com.hotpads.job.web.JobCategory;

public class NoOpJobConfig extends JobConfigModule{

	@Override
	protected Class<? extends LongRunningTaskDao> getLongRunningTaskDaoClass(){
		return EmptyLongRunningTaskDao.class;
	}

	@Override
	protected Class<? extends TriggerGroup> getTriggerGroupClass(){
		return EmptyTriggerGroup.class;
	}

	@Override
	protected JobCategory getDefaultJobCategory(){
		return EmptyJobCategory.NONE;
	}

}
