package com.hotpads.webappinstance.job;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.job.trigger.BaseJob;
import com.hotpads.job.trigger.JobEnvironment;
import com.hotpads.webappinstance.WebAppInstanceDao;
import com.hotpads.webappinstance.WebAppInstanceNodes;
import com.hotpads.webappinstance.databean.WebAppInstanceKey;
import com.hotpads.webappinstance.setting.WebAppInstanceSettings;

public class WebAppInstanceVacuumJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(WebAppInstanceVacuumJob.class);

	private final WebAppInstanceSettings webAppInstanceSettings;
	private final WebAppInstanceNodes webAppInstanceNodes;
	private final WebAppInstanceDao webAppInstanceDao;

	@Inject
	public WebAppInstanceVacuumJob(JobEnvironment jobEnvironment, WebAppInstanceSettings webAppInstanceSettings,
			WebAppInstanceNodes webAppInstanceNodes, WebAppInstanceDao webAppInstanceDao){
		super(jobEnvironment);
		this.webAppInstanceSettings = webAppInstanceSettings;
		this.webAppInstanceNodes = webAppInstanceNodes;
		this.webAppInstanceDao = webAppInstanceDao;
	}

	@Override
	public void run(){
		if(tracker.heartbeat().isStopRequested()){
			return;
		}
		List<WebAppInstanceKey> inactiveWebAppInstanceKeys = DatabeanTool.getKeys(webAppInstanceDao
				.findInactiveWebAppInstances());
		logger.warn("deleting inactiveWebAppKeys:" + inactiveWebAppInstanceKeys);
		webAppInstanceNodes.getWebAppInstance().deleteMulti(inactiveWebAppInstanceKeys, null);
	}

	@Override
	public boolean shouldRun(){
		return webAppInstanceSettings.getRunWebAppInstanceVacuum().getValue();
	}
}
