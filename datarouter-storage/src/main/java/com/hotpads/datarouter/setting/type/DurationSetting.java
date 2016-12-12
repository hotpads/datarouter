package com.hotpads.datarouter.setting.type;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.util.core.Duration;

public interface DurationSetting extends Setting<Duration>{

	@Override
	default Duration parseStringValue(String stringValue){
		return new Duration(stringValue);
	}

}
