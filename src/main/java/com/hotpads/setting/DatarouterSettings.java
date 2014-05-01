package com.hotpads.setting;

import javax.inject.Inject;

import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;

public class DatarouterSettings extends SettingNode {

	@Inject
	public DatarouterSettings(ClusterSettingFinder finder) {
		super(finder, "datarouter.", "");
	}

}
