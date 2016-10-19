package com.hotpads.datarouter.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.batch.config.BatchSizeOptimizerSettings;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.profile.ProfilingSettings;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;
import com.hotpads.datarouter.storage.failover.FailoverSettings;

@Singleton
public class DatarouterClusterSettings extends SettingNode implements DatarouterSettings {

	private final Setting<Boolean> loggingConfigUpdaterEnabled;
	private final Setting<Boolean> recordCallsites;
	private final Setting<Integer> numThreadsForMaxThreadsTest;


	@Inject
	public DatarouterClusterSettings(SettingFinder finder, DatarouterNotificationSettings notificationSettings,
			ProfilingSettings profilingSettings, BatchSizeOptimizerSettings batchSizeOptimizerSettings,
			NodeWatchSettings nodeWatchSettings, ClientAvailabilitySettings clientAvailabilitySettings,
			FailoverSettings failoverSettings){
		super(finder, "datarouter.", "");
		registerChild(notificationSettings);
		registerChild(profilingSettings);
		registerChild(batchSizeOptimizerSettings);
		registerChild(clientAvailabilitySettings);
		registerChild(nodeWatchSettings);
		registerChild(failoverSettings);

		loggingConfigUpdaterEnabled = registerBoolean("loggingConfigUpdaterEnabled", true);
		recordCallsites = registerBoolean("recordCallsites", false);
		numThreadsForMaxThreadsTest = registerInteger("numThreadsForMaxThreadsTest", 1);
	}


	@Override
	public Setting<Boolean> getLoggingConfigUpdaterEnabled(){
		return loggingConfigUpdaterEnabled;
	}

	@Override
	public Setting<Boolean> getRecordCallsites(){
		return recordCallsites;
	}

	@Override
	public Setting<Integer> getNumThreadsForMaxThreadsTest(){
		return numThreadsForMaxThreadsTest;
	}

}
