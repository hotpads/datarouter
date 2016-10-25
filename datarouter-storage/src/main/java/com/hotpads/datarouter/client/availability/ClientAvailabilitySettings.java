package com.hotpads.datarouter.client.availability;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;
import com.hotpads.datarouter.setting.constant.ConstantBooleanSetting;

@Singleton
public class ClientAvailabilitySettings extends SettingNode{

	public static final String SETTING_PREFIX = "datarouter.availability.";
	public static final String READ = "read";
	public static final String WRITE = "write";

	private final Map<String,AvailabilitySettingNode> availabilityByClientName;

	private final DatarouterClients clients;
	private final SettingFinder finder;

	@Inject
	public ClientAvailabilitySettings(SettingFinder finder, DatarouterClients clients,
			ClientAvailabilitySwitchThresholdSettings clientAvailabilitySwitchThresholdSettings){
		super(finder, SETTING_PREFIX, "datarouter.");
		this.finder = finder;
		this.clients = clients;
		availabilityByClientName = new HashMap<>();

		registerChild(clientAvailabilitySwitchThresholdSettings);
	}

	public AvailabilitySettingNode getAvailabilityForClientName(String clientName){
		return availabilityByClientName.computeIfAbsent(clientName, name -> new AvailabilitySettingNode(name,
				clients.getClientId(name).getDisableable()));
	}

	public class AvailabilitySettingNode extends SettingNode{

		public final Setting<Boolean> read;
		public final Setting<Boolean> write;

		public AvailabilitySettingNode(String clientName, boolean disableable){
			super(finder, ClientAvailabilitySettings.this.getName() + clientName + ".",
					ClientAvailabilitySettings.this.getName());

			if(disableable){
				ClientAvailabilitySettings.this.registerChild(this);
				read = registerBoolean(READ, true);
				write = registerBoolean(WRITE, true);
			}else{
				read = ConstantBooleanSetting.TRUE;
				write = ConstantBooleanSetting.TRUE;
			}

		}

	}

}