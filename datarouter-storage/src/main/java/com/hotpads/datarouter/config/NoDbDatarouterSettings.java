package com.hotpads.datarouter.config;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.constant.ConstantBooleanSetting;
import com.hotpads.datarouter.setting.constant.ConstantIntegerSetting;

public class NoDbDatarouterSettings implements DatarouterSettings{

	@Override
	public Setting<Boolean> getLoggingConfigUpdaterEnabled(){
		return new ConstantBooleanSetting(true);
	}

	@Override
	public Setting<Boolean> getRecordCallsites(){
		return new ConstantBooleanSetting(false);
	}

	@Override
	public Setting<Integer> getNumThreadsForMaxThreadsTest(){
		return new ConstantIntegerSetting(1);
	}

}
