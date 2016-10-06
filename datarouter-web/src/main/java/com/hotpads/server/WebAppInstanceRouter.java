package com.hotpads.server;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.server.databean.WebAppInstance;
import com.hotpads.server.databean.WebAppInstance.WebAppFielder;
import com.hotpads.server.databean.WebAppInstanceKey;

@Singleton
public class WebAppInstanceRouter extends BaseRouter {

	private static final String NAME = "webAppInstances";

	public static final ClientId CLIENT_config = new ClientId("config", true);

	private static final List<ClientId> CLIENT_IDS = Arrays.asList(CLIENT_config);

	@Override
	public List<ClientId> getClientIds(){
		return CLIENT_IDS;
	}

	/********************************** nodes ****************************************/
	public final SortedMapStorageNode<WebAppInstanceKey,WebAppInstance> webApp;

	@Inject
	public WebAppInstanceRouter(Datarouter datarouter, DatarouterProperties datarouterProperties, NodeFactory
			nodeFactory, DatarouterSettings datarouterSettings){
		super(datarouter, datarouterProperties.getConfigPath(), NAME, nodeFactory, datarouterSettings);

		webApp = createAndRegister(CLIENT_config, WebAppInstance::new, WebAppFielder::new);
	}
}
