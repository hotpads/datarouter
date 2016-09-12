package com.hotpads.datarouter.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;
import com.hotpads.util.core.cache.Cached;

@Singleton
public class NodeWatchSettings extends SettingNode{

	private final Cached<Boolean> tableSamplerJob;
	private final Cached<Boolean> tableRowCountJob;
	private final Cached<Boolean> tableSizeMonitoringJob;

	@Inject
	public NodeWatchSettings(SettingFinder finder){
		super(finder, "datarouter.nodewatch.", "datarouter.");
		tableSamplerJob = registerBoolean("tableSampler", false);
		tableRowCountJob = registerBoolean("tableRowCount", false);
		tableSizeMonitoringJob = registerBoolean("tableSizeMonitoringJob", false);
	}

	public Cached<Boolean> getTableSamplerJob(){
		return tableSamplerJob;
	}

	public Cached<Boolean> getTableRowCount(){
		return tableRowCountJob;
	}

	public Cached<Boolean> getTableSizeMonitoringJob(){
		return tableSizeMonitoringJob;
	}

}
