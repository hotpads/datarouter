package com.hotpads.job.config.setting.job;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.job.setting.ClusterSettingFinder;
import com.hotpads.job.setting.Setting;
import com.hotpads.job.setting.SettingNode;
import com.hotpads.job.setting.cached.imp.BooleanCachedSetting;

@Singleton
public class FraudSettings extends SettingNode{

	protected Setting<Boolean> fraudBrainTrainingDataSeederJob = new BooleanCachedSetting(finder, 
			getName()+"fraudBrainTrainingDataSeederJob", false);
	@Inject
	public FraudSettings(ClusterSettingFinder finder){
		super(finder, "job.fraud.", "job.");
		register(fraudBrainTrainingDataSeederJob);
	}


	public Setting<Boolean> getFraudBrainTrainingDataSeederJob(){
		return fraudBrainTrainingDataSeederJob;
	}
	
}