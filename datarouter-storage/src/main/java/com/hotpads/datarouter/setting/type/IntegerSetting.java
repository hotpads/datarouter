package com.hotpads.datarouter.setting.type;

import com.hotpads.datarouter.setting.Setting;

public interface IntegerSetting extends Setting<Integer>{

	@Override
	default Integer parseStringValue(String stringValue){
		return Integer.valueOf(stringValue);
	}

}
