package com.hotpads.joblet;

import javax.inject.Inject;

import com.hotpads.joblet.execute.ParallelJobletProcessors;
import com.hotpads.listener.DatarouterAppListener;

/*
 * To run joblets, add this AppListener to your WebApp
 */
public class JobletAppListener extends DatarouterAppListener{

	@Inject
	private ParallelJobletProcessors parallelJobletProcessors;//don't delete me!  creating me starts the joblets

	@Override
	protected void onStartUp(){
	}

	@Override
	protected void onShutDown(){
		parallelJobletProcessors.requestShutdown();

	}

}
