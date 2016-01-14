package com.hotpads.datarouter.config;

import com.hotpads.datarouter.setting.cached.impl.BooleanCachedSetting;
import com.hotpads.datarouter.setting.cached.impl.IntegerCachedSetting;

public interface DatarouterSettings{

	BooleanCachedSetting getLoggingConfigUpdaterEnabled();

	BooleanCachedSetting getRecordCallsites();

	IntegerCachedSetting getNumThreadsForMaxThreadsTest();

}