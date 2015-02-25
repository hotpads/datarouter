package com.hotpads.job.trigger;

import com.google.inject.AbstractModule;
import com.hotpads.job.record.LongRunningTaskDao;
import com.hotpads.job.web.JobCategory;

public abstract class JobConfigModule extends AbstractModule{

	@Override
	protected final void configure(){
		bind(TriggerGroup.class).to(getTriggerGroupClass());
		bind(LongRunningTaskDao.class).to(getLongRunningTaskDaoClass());
		bind(JobCategory.class).toInstance(getDefaultJobCategory());
	}

	protected abstract JobCategory getDefaultJobCategory();

	protected abstract Class<? extends LongRunningTaskDao> getLongRunningTaskDaoClass();

	protected abstract Class<? extends TriggerGroup> getTriggerGroupClass();

}
