package com.hotpads.clustersetting;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage;

public interface ClusterSettingNodes{

	public SortedMapStorage<ClusterSettingKey,ClusterSetting> clusterSetting();
	public SortedMapStorage<ClusterSettingLogKey,ClusterSettingLog> clusterSettingLog();

}
