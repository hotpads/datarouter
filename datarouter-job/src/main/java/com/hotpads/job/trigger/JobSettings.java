package com.hotpads.job.trigger;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterClusterSettings;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingRoot;

@Singleton
public class JobSettings extends SettingRoot{

	private final Setting<Boolean> saveLongRunningTasks;
	private final Setting<Boolean> scheduleMissedJobsOnStartup;
	private final Setting<Boolean> processJobs;


	@Inject
	public JobSettings(SettingFinder finder, DatarouterClusterSettings datarouterSettings){
		super(finder, "datarouterJob.", "");
		dependsOn(datarouterSettings);

		this.saveLongRunningTasks = registerBoolean("saveLongRunningTasks", false);
		this.scheduleMissedJobsOnStartup = registerBoolean("scheduleMissedJobsOnStartup", false);
		this.processJobs = registerBoolean("processJobs", true);
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
