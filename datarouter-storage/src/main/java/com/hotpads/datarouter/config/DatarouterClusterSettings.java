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

	private BooleanCachedSetting loggingConfigUpdaterEnabled;
	private BooleanCachedSetting recordCallsites;
	private IntegerCachedSetting numThreadsForMaxThreadsTest;

	@Inject
	public DatarouterClusterSettings(SettingFinder finder, DatarouterNotificationSettings notificationSettings,
			ProfilingSettings profilingSettings, BatchSizeOptimizerSettings batchSizeOptimizerSettings,
			ClientAvailabilityClusterSettings clientAvailabilitySettings){
		super(finder, "datarouter.", "");
		registerChild(notificationSettings);
		registerChild(profilingSettings);
		registerChild(batchSizeOptimizerSettings);
		registerChild(clientAvailabilitySettings);
		registerSettings();
	}

	private void registerSettings(){
		this.loggingConfigUpdaterEnabled = registerBoolean("loggingConfigUpdaterEnabled", true);
		this.recordCallsites = registerBoolean("recordCallsites", false);
		this.numThreadsForMaxThreadsTest = registerInteger("numThreadsForMaxThreadsTest", 1);
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
