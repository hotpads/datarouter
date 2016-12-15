package com.hotpads.datarouter.setting.type;

import com.hotpads.datarouter.setting.Setting;

public interface DoubleSetting extends Setting<Double>{

	@Override
	default Double parseStringValue(String stringValue){
		return Double.valueOf(stringValue);
	}

}
