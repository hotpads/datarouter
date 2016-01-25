package com.hotpads.job.trigger;

import com.google.inject.AbstractModule;
import com.hotpads.job.record.LongRunningTaskNodeProvider;
import com.hotpads.job.web.JobCategory;

public abstract class JobConfigModule extends AbstractModule{

	@Override
	protected final void configure(){
		bind(TriggerGroup.class).to(getTriggerGroupClass());
		bind(LongRunningTaskNodeProvider.class).to(getLongRunningTaskNodeProviderClass());
		bind(JobCategory.class).toInstance(getDefaultJobCategory());		
	}

	protected abstract JobCategory getDefaultJobCategory();

	protected abstract Class<? extends LongRunningTaskNodeProvider> getLongRunningTaskNodeProviderClass();

	protected abstract Class<? extends TriggerGroup> getTriggerGroupClass();		

}
