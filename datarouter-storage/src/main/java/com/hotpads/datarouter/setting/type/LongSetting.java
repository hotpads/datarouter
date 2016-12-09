package com.hotpads.datarouter.setting.type;

import com.hotpads.datarouter.setting.Setting;

public interface LongSetting extends Setting<Long>{

	@Override
	default Long parseStringValue(String stringValue){
		return Long.valueOf(stringValue);
	}

}
