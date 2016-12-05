package com.hotpads.joblet;

import javax.inject.Inject;

import com.hotpads.joblet.execute.JobletProcessors;
import com.hotpads.listener.DatarouterAppListener;

/*
 * To run joblets, add this AppListener to your WebApp
 */
public class JobletAppListener extends DatarouterAppListener{

	@Inject
	private JobletProcessors jobletProcessors;

	@Override
	protected void onStartUp(){
		jobletProcessors.createAndStartProcessors();
	}

	@Override
	protected void onShutDown(){
		jobletProcessors.requestShutdown();
	}

}
