package com.hotpads.datarouter.client.availability;

import com.hotpads.setting.Setting;

public interface ClientAvailabilitySettings{

	Setting<Boolean> getAvailabilityForClientName(String clientName);

}
