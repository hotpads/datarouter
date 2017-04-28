package com.hotpads.job.trigger;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterClusterSettings;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingRoot;

@Singleton
public class JobSettings extends SettingRoot{

	public final Setting<Boolean> saveLongRunningTasks;
	public final Setting<Boolean> runLongRunningTaskVacuum;
	public final Setting<Boolean> scheduleMissedJobsOnStartup;
	public final Setting<Boolean> processJobs;


	@Inject
	public JobSettings(SettingFinder finder, DatarouterClusterSettings datarouterSettings){
		super(finder, "datarouterJob.", "");
		dependsOn(datarouterSettings);

		this.saveLongRunningTasks = registerBoolean("saveLongRunningTasks", false);
		this.runLongRunningTaskVacuum = registerBoolean("runLongRunningTaskVacuum", false);
		this.scheduleMissedJobsOnStartup = registerBoolean("scheduleMissedJobsOnStartup", false);
		this.processJobs = registerBoolean("processJobs", true);
	}

}
