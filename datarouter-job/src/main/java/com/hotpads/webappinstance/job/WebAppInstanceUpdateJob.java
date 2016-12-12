package com.hotpads.webappinstance.job;

import javax.inject.Inject;

import com.hotpads.job.trigger.BaseJob;
import com.hotpads.job.trigger.JobEnvironment;
import com.hotpads.webappinstance.WebAppInstanceDao;

public class WebAppInstanceUpdateJob extends BaseJob{

	private final WebAppInstanceDao webAppInstanceDao;

	@Inject
	public WebAppInstanceUpdateJob(JobEnvironment jobEnvironment, WebAppInstanceDao webAppInstanceDao){
		super(jobEnvironment);
		this.webAppInstanceDao = webAppInstanceDao;
	}

	@Override
	public boolean shouldRun(){
		return true;
	}

	@Override
	public void run() {
		webAppInstanceDao.updateWebAppInstanceTable();
	}
}
