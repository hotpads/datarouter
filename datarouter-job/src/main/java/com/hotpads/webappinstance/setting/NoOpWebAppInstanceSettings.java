package com.hotpads.webappinstance.setting;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.constant.ConstantBooleanSetting;

public class NoOpWebAppInstanceSettings implements WebAppInstanceSettings{

	@Override
	public Setting<Boolean> getRunWebAppInstanceVacuum(){
		return ConstantBooleanSetting.FALSE;
	}

}
