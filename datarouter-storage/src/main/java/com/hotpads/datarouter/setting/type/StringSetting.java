package com.hotpads.datarouter.setting.type;

import com.hotpads.datarouter.setting.Setting;

public interface StringSetting extends Setting<String>{

	@Override
	default String parseStringValue(String stringValue){
		return stringValue;
	}

}
