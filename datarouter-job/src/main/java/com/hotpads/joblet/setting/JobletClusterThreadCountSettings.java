package com.hotpads.joblet.setting;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.app.WebAppName;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.joblet.type.JobletTypeFactory;

@Singleton
public class JobletClusterThreadCountSettings extends BaseJobletThreadCountSettings{

	public static final String NAME = "clusterThreadCount";


	@Inject
	public JobletClusterThreadCountSettings(SettingFinder finder, WebAppName webAppName,
			JobletTypeFactory jobletTypeFactory){
		super(finder, webAppName, jobletTypeFactory, NAME, 0);
	}

}