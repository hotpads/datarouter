package com.hotpads.job.trigger;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.WebAppName;
import com.hotpads.setting.Setting;
import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class JobSettings extends SettingNode{

	private Setting<Boolean> saveLongRunningTasks;
	private Setting<Boolean> scheduleMissedJobsOnStartup;
	private Setting<Boolean> processJobs;
	
	@Inject
	public JobSettings(ClusterSettingFinder finder, WebAppName webAppName){
		super(finder, webAppName + ".job.", webAppName + ".");
		registerSettings();
	}
	
	private void registerSettings(){
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
