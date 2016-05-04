package com.hotpads.job.trigger;

import com.google.inject.AbstractModule;
import com.hotpads.job.record.LongRunningTaskNodeProvider;

public abstract class JobConfigModule extends AbstractModule{

	@Override
	protected final void configure(){
		bind(LongRunningTaskNodeProvider.class).to(getLongRunningTaskNodeProviderClass());
	}

	protected abstract Class<? extends LongRunningTaskNodeProvider> getLongRunningTaskNodeProviderClass();

}
