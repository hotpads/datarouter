package com.hotpads.datarouter.config;

import com.hotpads.datarouter.setting.cached.impl.BooleanCachedSetting;
import com.hotpads.datarouter.setting.cached.impl.IntegerCachedSetting;

public class NoOpDatarouterSettings implements DatarouterSettings{

	@Override
	public BooleanCachedSetting getLoggingConfigUpdaterEnabled(){
		return null;
	}

	@Override
	public BooleanCachedSetting getRecordCallsites(){
		return null;
	}

	@Override
	public IntegerCachedSetting getNumThreadsForMaxThreadsTest(){
		return null;
	}

}
