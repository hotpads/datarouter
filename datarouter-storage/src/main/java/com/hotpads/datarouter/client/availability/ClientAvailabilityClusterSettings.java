package com.hotpads.datarouter.client.availability;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.setting.Setting;
import com.hotpads.setting.cluster.SettingFinder;
import com.hotpads.setting.cluster.SettingNode;
import com.hotpads.setting.constant.ConstantBooleanSetting;

@Singleton
public class ClientAvailabilityClusterSettings extends SettingNode implements ClientAvailabilitySettings{

	private final Map<String,Setting<Boolean>> availabilityByClientName;
	private final DatarouterClients clients;

	@Inject
	public ClientAvailabilityClusterSettings(SettingFinder finder, DatarouterClients clients){
		super(finder, "datarouter.availability.", "datarouter.");
		this.clients = clients;
		availabilityByClientName = new HashMap<>();
	}

	@Override
	public Setting<Boolean> getAvailabilityForClientName(String clientName){
		return availabilityByClientName.computeIfAbsent(clientName, name -> {
			if(clients.getDisableable(name)){
				return registerBoolean(name, true);
			}
			return new ConstantBooleanSetting(true);
		});
	}

}