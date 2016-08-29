package com.hotpads.joblet;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.joblet.execute.ParallelJobletProcessors;
import com.hotpads.listener.DatarouterAppListener;

/*
 * To run joblets, add this AppListener to your WebApp
 */
public class JobletAppListener extends DatarouterAppListener{
	private static final Logger logger = LoggerFactory.getLogger(JobletAppListener.class);

	@Inject
	private ParallelJobletProcessors parallelJobletProcessors;//don't delete me!  creating me starts the joblets

	@Override
	protected void onStartUp(){
		logger.warn("starting {}", parallelJobletProcessors.toString());
	}

	@Override
	protected void onShutDown(){
		parallelJobletProcessors.requestShutdown();

	}

}
