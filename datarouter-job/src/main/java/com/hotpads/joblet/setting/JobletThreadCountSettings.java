package com.hotpads.joblet.setting;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.app.WebAppName;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.joblet.execute.JobletProcessor;
import com.hotpads.joblet.type.JobletTypeFactory;

@Singleton
public class JobletThreadCountSettings extends BaseJobletThreadCountSettings{

	public static final String NAME = "threadCount";

	private static final int DEFAULT_NUM_THREADS = JobletProcessor.ENABLE_CLUSTER_THREAD_LIMITS ? 1 : 0;


	@Inject
	public JobletThreadCountSettings(SettingFinder finder, WebAppName webAppName, JobletTypeFactory jobletTypeFactory){
		super(finder, webAppName, jobletTypeFactory, NAME, DEFAULT_NUM_THREADS);
	}

}
