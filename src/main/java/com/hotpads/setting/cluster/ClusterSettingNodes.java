package com.hotpads.setting.cluster;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;

public interface ClusterSettingNodes{

	public SortedMapStorageNode<ClusterSettingKey,ClusterSetting> clusterSetting();
	
}
