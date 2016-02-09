package com.hotpads.datarouter.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.batch.config.BatchSizeOptimizerSettings;
import com.hotpads.datarouter.client.availability.ClientAvailabilityClusterSettings;
import com.hotpads.datarouter.profile.ProfilingSettings;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;
import com.hotpads.datarouter.setting.cached.impl.BooleanCachedSetting;
import com.hotpads.datarouter.setting.cached.impl.IntegerCachedSetting;

@Singleton
public class DatarouterClusterSettings extends SettingNode implements DatarouterSettings {

	private final BooleanCachedSetting loggingConfigUpdaterEnabled;
	private final BooleanCachedSetting recordCallsites;
	private final IntegerCachedSetting numThreadsForMaxThreadsTest;


	@Inject
	public DatarouterClusterSettings(SettingFinder finder, DatarouterNotificationSettings notificationSettings,
			ProfilingSettings profilingSettings, BatchSizeOptimizerSettings batchSizeOptimizerSettings,
			ClientAvailabilityClusterSettings clientAvailabilitySettings){
		super(finder, "datarouter.", "");
		registerChild(notificationSettings);
		registerChild(profilingSettings);
		registerChild(batchSizeOptimizerSettings);
		registerChild(clientAvailabilitySettings);

		loggingConfigUpdaterEnabled = registerBoolean("loggingConfigUpdaterEnabled", true);
		recordCallsites = registerBoolean("recordCallsites", false);
		numThreadsForMaxThreadsTest = registerInteger("numThreadsForMaxThreadsTest", 1);
	}


	@Override
	public BooleanCachedSetting getLoggingConfigUpdaterEnabled(){
		return loggingConfigUpdaterEnabled;
	}

	@Override
	public BooleanCachedSetting getRecordCallsites(){
		return recordCallsites;
	}

	@Override
	public IntegerCachedSetting getNumThreadsForMaxThreadsTest(){
		return numThreadsForMaxThreadsTest;
	}

}
