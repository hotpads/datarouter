package com.hotpads.joblet.queue;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.joblet.enums.JobletQueueMechanism;
import com.hotpads.joblet.setting.JobletSettings;

@Singleton
public class JobletRequestSelectorFactory{

	@Inject
	private JobletSettings jobletSettings;
	@Inject
	private DatarouterInjector injector;

	public JobletRequestSelector create(){
		JobletQueueMechanism queueMechanism = jobletSettings.getQueueMechanismEnum();
		return injector.getInstance(queueMechanism.getSelectorClass());
	}

}
