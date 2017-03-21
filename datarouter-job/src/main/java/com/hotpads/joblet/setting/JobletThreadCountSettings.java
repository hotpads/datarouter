package com.hotpads.joblet.setting;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.joblet.type.JobletTypeFactory;

@Singleton
public class JobletThreadCountSettings extends BaseJobletThreadCountSettings{

	public static final String NAME = "threadCount";


	@Inject
	public JobletThreadCountSettings(SettingFinder finder, JobletTypeFactory jobletTypeFactory){
		super(finder, jobletTypeFactory, NAME, 1);
	}

}
