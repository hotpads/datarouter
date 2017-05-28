package com.hotpads.handler.account;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.handler.account.DatarouterAccount.DatarouterAccountFielder;
import com.hotpads.handler.account.DatarouterUserAccountMap.DatarouterUserAccountMapFielder;

@Singleton
public class DatarouterAccountRouter extends BaseRouter implements DatarouterAccountNodes{

	public static interface DatarouterAccountRouterParamsProvider{
		DatarouterAccountRouterParams getDatarouterAccountRouterParams();
	}

	public static class DatarouterAccountRouterParams{
		private final String configFileLocation;
		private final ClientId clientId;

		public DatarouterAccountRouterParams(String configFileLocation, ClientId clientId){
			this.configFileLocation = configFileLocation;
			this.clientId = clientId;
		}
	}

	private static final String NAME = "datarouterAccount";

	private final SortedMapStorage<DatarouterAccountKey,DatarouterAccount> datarouterAccount;
	private final SortedMapStorage<DatarouterUserAccountMapKey,DatarouterUserAccountMap> datarouterUserAccountMap;

	@Inject
	public DatarouterAccountRouter(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterSettings datarouterSettings, DatarouterAccountRouterParamsProvider paramsProvider){
		super(datarouter, paramsProvider.getDatarouterAccountRouterParams().configFileLocation, NAME, nodeFactory,
				datarouterSettings);
		DatarouterAccountRouterParams params = paramsProvider.getDatarouterAccountRouterParams();
		datarouterAccount = createAndRegister(params.clientId, DatarouterAccount::new, DatarouterAccountFielder::new);
		datarouterUserAccountMap = createAndRegister(params.clientId, DatarouterUserAccountMap::new,
				DatarouterUserAccountMapFielder::new);
	}

	@Override
	public SortedMapStorage<DatarouterAccountKey,DatarouterAccount> datarouterAccount(){
		return datarouterAccount;
	}

	@Override
	public SortedMapStorage<DatarouterUserAccountMapKey,DatarouterUserAccountMap> datarouterUserAccountMap(){
		return datarouterUserAccountMap;
	}

}
