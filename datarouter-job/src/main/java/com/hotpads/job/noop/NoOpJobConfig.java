package com.hotpads.job.noop;

import com.hotpads.job.record.LongRunningTaskNodeProvider;
import com.hotpads.job.trigger.JobConfigModule;

public class NoOpJobConfig extends JobConfigModule{

	@Override
	protected Class<? extends LongRunningTaskNodeProvider> getLongRunningTaskNodeProviderClass(){
		return EmptyLongRunningTaskNodeProvider.class;
	}

}
