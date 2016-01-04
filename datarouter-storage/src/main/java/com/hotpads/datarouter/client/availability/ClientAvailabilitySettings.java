package com.hotpads.datarouter.client.availability;

import com.hotpads.datarouter.setting.Setting;

public interface ClientAvailabilitySettings{

	Setting<Boolean> getAvailabilityForClientName(String clientName);

}
