package com.hotpads.joblet.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;
import com.hotpads.joblet.type.JobletType;
import com.hotpads.joblet.type.JobletTypeFactory;

public class BaseJobletThreadCountSettings extends SettingNode{

	private final Map<JobletType<?>,Setting<Integer>> settingByJobletType = new HashMap<>();

	public BaseJobletThreadCountSettings(SettingFinder finder, JobletTypeFactory jobletTypeFactory, String nodeName,
			int defaultNumThreads){
		super(finder, "datarouterJob.joblet." + nodeName + ".", "datarouterJob.joblet.");

		for(JobletType<?> jobletType : jobletTypeFactory.getAllTypes()){
			Setting<Integer> setting = registerSetting(jobletType, jobletType.getPersistentString(), defaultNumThreads);
			settingByJobletType.put(jobletType, setting);
		}
	}

	/*-------------- methods ----------------------*/

	public Setting<Integer> registerSetting(JobletType<?> jobletType, String name, Integer defaultValue){
		Setting<Integer> setting = registerInteger(name, defaultValue);
		settingByJobletType.put(jobletType, setting);
		return setting;
	}

	public Setting<Integer> getSettingForJobletType(JobletType<?> type){
		return settingByJobletType.get(type);
	}

	public int getCountForJobletType(JobletType<?> type){
		return Optional.ofNullable(settingByJobletType.get(type).getValue()).orElse(0);
	}

}
