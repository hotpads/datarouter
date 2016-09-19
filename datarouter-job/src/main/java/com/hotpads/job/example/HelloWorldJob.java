package com.hotpads.job.example;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.job.trigger.BaseJob;
import com.hotpads.job.trigger.JobEnvironment;

public class HelloWorldJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(HelloWorldJob.class);

	@Inject
	public HelloWorldJob(JobEnvironment jobEnvironment){
		super(jobEnvironment);
	}

	@Override
	public boolean shouldRun(){
		return true;
	}

	@Override
	public void run(){
		logger.info("hello world");
	}

}
