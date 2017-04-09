package com.hotpads.datarouter.client.imp.memcached.test;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.imp.memcached.MemcachedClientType;
import com.hotpads.datarouter.client.imp.memcached.node.MemcachedNode;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.profile.tally.Tally;
import com.hotpads.datarouter.profile.tally.Tally.TallyFielder;
import com.hotpads.datarouter.profile.tally.TallyKey;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterTestClientIds;
import com.hotpads.datarouter.test.TestDatarouterProperties;

@Singleton
public class TallyTestRouter extends BaseRouter{

	private static final int VERSION_Tally = 2;

	private final DatarouterClients datarouterClients;

	/********************************** nodes ********************************/

	private final MemcachedNode<TallyKey,Tally,TallyFielder> tallyNode;

	/******************************* constructor *****************************/

	@Inject
	public TallyTestRouter(TestDatarouterProperties datarouterProperties, Datarouter datarouter,
			DatarouterClients datarouterClients, NodeFactory nodeFactory, DatarouterSettings datarouterSettings){
		super(datarouter, datarouterProperties.getTestRouterConfigFileLocation(), TallyTestRouter.class.getSimpleName(),
				nodeFactory, datarouterSettings);

		this.datarouterClients = datarouterClients;
		this.tallyNode = buildTallyNode(DatarouterTestClientIds.CLIENT_drTestMemcached);
	}


	/*************************** get/set *************************************/

	public MemcachedNode<TallyKey,Tally,TallyFielder> tally(){
		return tallyNode;
	}

	/********************************** helper *******************************/

	private MemcachedNode<TallyKey, Tally, TallyFielder> buildTallyNode(ClientId clientId){
		String clientName = clientId.getName();
		MemcachedClientType clientType = (MemcachedClientType) datarouterClients.getClientTypeInstance(clientName);
		Objects.requireNonNull(clientType, "clientType not found for clientName:" + clientName);
		NodeParams<TallyKey,Tally,TallyFielder> params = new NodeParamsBuilder<>(this, Tally::new, TallyFielder::new)
				.withClientId(clientId)
				.withSchemaVersion(VERSION_Tally)
				.build();
		return register(clientType.createNodeWithoutAdapters(params));
	}
}