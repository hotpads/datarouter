package com.hotpads.joblet.execute;

import javax.inject.Inject;

import com.hotpads.listener.DatarouterAppListener;

public class JobletAppListener extends DatarouterAppListener{

	@Inject
	private ParallelJobletProcessors parallelJobletProcessors;

	@Override
	protected void onStartUp(){
	}

	@Override
	protected void onShutDown(){
		parallelJobletProcessors.requestShutdown();
	}

}
