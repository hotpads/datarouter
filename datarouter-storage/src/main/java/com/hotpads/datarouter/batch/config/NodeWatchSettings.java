package com.hotpads.datarouter.batch.config;

import javax.inject.Inject;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;
import com.hotpads.util.core.cache.Cached;

public class NodeWatchSettings extends SettingNode{

	private final Cached<Boolean> tableRowCountJob;

	@Inject
	public NodeWatchSettings(SettingFinder finder){
		super(finder, "datarouter.nodewatch.", "datarouter.");
		tableRowCountJob = registerBoolean("tableRowCount", false);
	}

	public Cached<Boolean> getTableRowCount(){
		return tableRowCountJob;
	}

}
