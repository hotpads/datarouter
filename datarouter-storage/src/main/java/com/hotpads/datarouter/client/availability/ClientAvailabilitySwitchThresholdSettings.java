package com.hotpads.datarouter.client.availability;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;

@Singleton
public class ClientAvailabilitySwitchThresholdSettings extends SettingNode{

	private final Map<String,Setting<Integer>> switchThresholdByClientName;

	@Inject
	public ClientAvailabilitySwitchThresholdSettings(SettingFinder finder){
		super(finder, "datarouter.availability.switchThreshold.", "datarouter.availability.");

		this.switchThresholdByClientName = new HashMap<>();
	}

	public Setting<Integer> getSwitchThreshold(String clientName){
		return switchThresholdByClientName.computeIfAbsent(clientName, name -> registerInteger(name, 0));
	}

}
