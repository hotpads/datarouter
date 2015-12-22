package com.hotpads.setting.cluster;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage;

public interface ClusterSettingNodes{

	public SortedMapStorage<ClusterSettingKey,ClusterSetting> clusterSetting();

}
