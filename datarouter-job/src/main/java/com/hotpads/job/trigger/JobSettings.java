package com.hotpads.job.trigger;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.WebAppName;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;

@Singleton
public class JobSettings extends SettingNode{

	private final Setting<Boolean> saveLongRunningTasks;
	private final Setting<Boolean> scheduleMissedJobsOnStartup;
	private final Setting<Boolean> processJobs;


	@Inject
	public JobSettings(SettingFinder finder, WebAppName webAppName){
		super(finder, webAppName + ".job.", webAppName + ".");

		saveLongRunningTasks = registerBoolean("saveLongRunningTasks", false);
		scheduleMissedJobsOnStartup = registerBoolean("scheduleMissedJobsOnStartup", false);
		processJobs = registerBoolean("processJobs", true);
	}


	public Setting<Boolean> getSaveLongRunningTasks(){
		return saveLongRunningTasks;
	}

	public Setting<Boolean> getScheduleMissedJobsOnStartup(){
		return scheduleMissedJobsOnStartup;
	}

	public Setting<Boolean> getProcessJobs(){
		return processJobs;
	}

}