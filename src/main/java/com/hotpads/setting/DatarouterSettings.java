package com.hotpads.setting;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class DatarouterSettings extends SettingNode {

	@Inject
	public DatarouterSettings(ClusterSettingFinder finder) {
		super(finder, "datarouter.", "");
	}

}
