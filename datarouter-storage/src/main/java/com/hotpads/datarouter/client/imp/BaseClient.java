package com.hotpads.datarouter.client.imp;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.setting.Setting;

public abstract class BaseClient
implements Client{

	private final String name;
	private final Setting<Boolean> isAvailable;

	public BaseClient(String name, ClientAvailabilitySettings clientAvailabilitySettings){
		this.name = name;
		this.isAvailable = clientAvailabilitySettings.getAvailabilityForClientName(getName());
	}

	/**************************** standard ******************************/

	@Override
	public int compareTo(Client client){
		return DrComparableTool.nullFirstCompareTo(getName(), client.getName());
	}

	@Override
	public boolean isAvailable(){
		return isAvailable.getValue();
	}

	@Override
	public String getName(){
		return name;
	}
}
