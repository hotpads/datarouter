package com.hotpads.webappinstance;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.webappinstance.databean.WebAppInstance;
import com.hotpads.webappinstance.databean.WebAppInstance.WebAppInstanceFielder;
import com.hotpads.webappinstance.databean.WebAppInstanceKey;

@Singleton
public class WebAppInstanceRouter extends BaseRouter implements WebAppInstanceNodes{

	public static class WebAppInstanceRouterParams{
		private final String configFileLocation;
		private final ClientId clientId;

		public WebAppInstanceRouterParams(String configFileLocation, ClientId clientId){
			this.configFileLocation = configFileLocation;
			this.clientId = clientId;
		}
	}

	private static final String NAME = "webAppInstance";

	private SortedMapStorageNode<WebAppInstanceKey,WebAppInstance> webAppInstance;

	@Inject
	public WebAppInstanceRouter(Datarouter datarouter, NodeFactory nodeFactory, DatarouterSettings datarouterSettings,
			WebAppInstanceRouterParams params){
		super(datarouter, params.configFileLocation, NAME, nodeFactory,
				datarouterSettings);
		webAppInstance = createAndRegister(params.clientId, WebAppInstance::new, WebAppInstanceFielder::new);
	}

	@Override
	public SortedMapStorageNode<WebAppInstanceKey,WebAppInstance> getWebAppInstance(){
		return webAppInstance;
	}

}
