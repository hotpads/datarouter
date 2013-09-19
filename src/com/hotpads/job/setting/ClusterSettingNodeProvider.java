package com.hotpads.job.setting;

import javax.inject.Inject;
import javax.inject.Provider;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;

@Deprecated//haven't gotten this to work
public class ClusterSettingNodeProvider implements Provider<SortedMapStorageNode<ClusterSettingKey,ClusterSetting>>{

	protected JobRouters jobsRouters;
	
	
	@Inject
	public ClusterSettingNodeProvider(JobRouters jobsRouters) {
		this.jobsRouters = jobsRouters;
	}


	@Override
	public SortedMapStorageNode<ClusterSettingKey,ClusterSetting> get() {
		return jobsRouters.config().clusterSetting;
	}
}
