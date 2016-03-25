package com.hotpads.datarouter.config;

import javax.inject.Inject;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;
import com.hotpads.util.core.cache.Cached;

public class NodeWatchSettings extends SettingNode{

	private final Cached<Boolean> tableRowCountJob;
	private final Cached<Boolean> tableSizeMonitoringJob;

	@Inject
	public NodeWatchSettings(SettingFinder finder){
		super(finder, "datarouter.nodewatch.", "datarouter.");
		tableRowCountJob = registerBoolean("tableRowCount", false);
		tableSizeMonitoringJob = registerBoolean("tableSizeMonitoringJob", false);
	}

	public Cached<Boolean> getTableRowCount(){
		return tableRowCountJob;
	}

	public Cached<Boolean> getTableSizeMonitoringJob(){
		return tableSizeMonitoringJob;
	}

}
