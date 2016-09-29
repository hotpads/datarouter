package com.hotpads.datarouter.config;

import com.hotpads.datarouter.setting.Setting;

public interface DatarouterSettings{

	Setting<Boolean> getLoggingConfigUpdaterEnabled();

	Setting<Boolean> getRecordCallsites();

	Setting<Integer> getNumThreadsForMaxThreadsTest();

}