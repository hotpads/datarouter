package com.hotpads.clustersetting;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.clustersetting.ClusterSetting.ClusterSettingFielder;
import com.hotpads.clustersetting.ClusterSettingLog.ClusterSettingLogFielder;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.NoDbDatarouterSettings;
import com.hotpads.datarouter.node.factory.SettinglessNodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;

@Singleton
public class ClusterSettingRouter extends BaseRouter implements ClusterSettingNodes{

	public static class ClusterSettingRouterParams{
		private final String configFileLocation;
		private final ClientId clientId;

		public ClusterSettingRouterParams(String configFileLocation, ClientId clientId){
			this.configFileLocation = configFileLocation;
			this.clientId = clientId;
		}
	}

	private static final String NAME = "clusterSetting";

	private final SortedMapStorage<ClusterSettingKey,ClusterSetting> clusterSetting;
	private final SortedMapStorage<ClusterSettingLogKey,ClusterSettingLog> clusterSettingLog;

	@Inject
	public ClusterSettingRouter(Datarouter datarouter, SettinglessNodeFactory settinglessNodeFactory,
			ClusterSettingRouterParams params){
		super(datarouter, params.configFileLocation, NAME, settinglessNodeFactory, new NoDbDatarouterSettings());
		clusterSetting = createAndRegister(params.clientId, ClusterSetting::new, ClusterSettingFielder::new);
		clusterSettingLog = createAndRegister(params.clientId, ClusterSettingLog::new, ClusterSettingLogFielder::new);
	}

	@Override
	public SortedMapStorage<ClusterSettingKey,ClusterSetting> clusterSetting(){
		return clusterSetting;
	}

	@Override
	public SortedMapStorage<ClusterSettingLogKey,ClusterSettingLog> clusterSettingLog(){
		return clusterSettingLog;
	}

}
