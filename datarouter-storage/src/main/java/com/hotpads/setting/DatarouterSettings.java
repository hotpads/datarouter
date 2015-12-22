package com.hotpads.setting;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.batch.config.BatchSizeOptimizerSettings;
import com.hotpads.datarouter.client.availability.ClientAvailabilityClusterSettings;
import com.hotpads.profile.ProfilingSettings;
import com.hotpads.setting.cached.imp.BooleanCachedSetting;
import com.hotpads.setting.cached.imp.IntegerCachedSetting;
import com.hotpads.setting.cluster.SettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class DatarouterSettings extends SettingNode {

	private BooleanCachedSetting loggingConfigUpdaterEnabled;
	private BooleanCachedSetting recordCallsites;
	private IntegerCachedSetting numThreadsForMaxThreadsTest;

	@Inject
	public DatarouterSettings(SettingFinder finder, DatarouterNotificationSettings notificationSettings,
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

	public BooleanCachedSetting getLoggingConfigUpdaterEnabled(){
		return loggingConfigUpdaterEnabled;
	}

	public BooleanCachedSetting getRecordCallsites(){
		return recordCallsites;
	}

	public IntegerCachedSetting getNumThreadsForMaxThreadsTest(){
		return numThreadsForMaxThreadsTest;
	}

}
