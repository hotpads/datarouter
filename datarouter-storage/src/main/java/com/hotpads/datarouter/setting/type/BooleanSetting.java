package com.hotpads.datarouter.setting.type;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.util.core.DrBooleanTool;

public interface BooleanSetting extends Setting<Boolean>{

	@Override
	default Boolean parseStringValue(String stringValue){
		return DrBooleanTool.isTrue(stringValue);
	}

}
