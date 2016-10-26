package com.hotpads.datarouter.config;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.constant.ConstantBooleanSetting;
import com.hotpads.datarouter.setting.constant.ConstantIntegerSetting;

public class NoDbDatarouterSettings implements DatarouterSettings{

	@Override
	public Setting<Boolean> getLoggingConfigUpdaterEnabled(){
		return ConstantBooleanSetting.TRUE;
	}

	@Override
	public Setting<Boolean> getRecordCallsites(){
		return ConstantBooleanSetting.FALSE;
	}

	@Override
	public Setting<Integer> getNumThreadsForMaxThreadsTest(){
		return new ConstantIntegerSetting(1);
	}

}
