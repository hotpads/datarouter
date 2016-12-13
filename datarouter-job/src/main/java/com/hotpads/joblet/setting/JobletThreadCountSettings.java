package com.hotpads.joblet.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.WebAppName;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;
import com.hotpads.joblet.type.JobletType;
import com.hotpads.joblet.type.JobletTypeFactory;

@Singleton
public class JobletThreadCountSettings extends SettingNode{

	public static final String NAME = "threadCount";

	private final Map<JobletType<?>,Setting<Integer>> settingByJobletType = new HashMap<>();

	@Inject
	public JobletThreadCountSettings(SettingFinder finder, WebAppName webAppName, JobletTypeFactory jobletTypeFactory){
		super(finder, webAppName + ".joblet.threadCount.", webAppName + ".joblet.");

		for(JobletType<?> jobletType : jobletTypeFactory.getAllTypes()){
			Setting<Integer> setting = registerThreadCountSetting(jobletType, jobletType.getPersistentString(), 0);
			settingByJobletType.put(jobletType, setting);
		}
	}

	/*-------------- methods ----------------------*/

	public Setting<Integer> registerThreadCountSetting(JobletType<?> jobletType, String name, Integer defaultValue){
		Setting<Integer> setting = registerInteger(name, defaultValue);
		settingByJobletType.put(jobletType, setting);
		return setting;
	}

	public Setting<Integer> getSettingForJobletType(JobletType<?> type){
		return settingByJobletType.get(type);
	}

	public int getThreadCountForJobletType(JobletType<?> type){
		return Optional.ofNullable(settingByJobletType.get(type).getValue()).orElse(0);
	}

}
