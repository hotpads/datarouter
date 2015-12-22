package com.hotpads.datarouter.client.availability;

import com.hotpads.setting.Setting;
import com.hotpads.setting.constant.ConstantBooleanSetting;

public class TestClientAvailabilitySettings implements ClientAvailabilitySettings{

	@Override
	public Setting<Boolean> getAvailabilityForClientName(String clientName){
		return new ConstantBooleanSetting(true);
	}

}
