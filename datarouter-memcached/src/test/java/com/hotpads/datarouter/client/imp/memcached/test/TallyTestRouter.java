package com.hotpads.datarouter.client.imp.memcached.test;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.imp.memcached.MemcachedClientType;
import com.hotpads.datarouter.client.imp.memcached.node.MemcachedNode;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.profile.tally.Tally;
import com.hotpads.datarouter.profile.tally.Tally.TallyFielder;
import com.hotpads.datarouter.profile.tally.TallyKey;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;

@Singleton
public class TallyTestRouter extends BaseRouter{

	private static final int VERSION_Tally = 2;

	private final List<ClientId> clientIds;

	private final MemcachedNode<TallyKey, Tally, TallyFielder> tallyNode;

	private DatarouterClients datarouterClients;

	@Inject
	public TallyTestRouter(Datarouter datarouter, DatarouterClients datarouterClients, NodeFactory nodeFactory,
			ClientId clientId, boolean useFielder){
		super(datarouter, DrTestConstants.CONFIG_PATH, TallyTestRouter.class.getSimpleName());
		this.datarouterClients = datarouterClients;
		this.clientIds = Arrays.asList(clientId);

		String clientName = clientId.getName();
		MemcachedClientType clientType = (MemcachedClientType) datarouterClients.getClientTypeInstance(clientName);
		Preconditions.checkNotNull(clientType, "clientType not found for clientName:"+clientName);

		NodeParamsBuilder<TallyKey, Tally, TallyFielder> paramsBuilder = new NodeParamsBuilder<TallyKey, Tally,
				TallyFielder>(this,Tally::new)
				.withClientId(clientId)
				.withFielder(TallyFielder::new)
				.withSchemaVersion(VERSION_Tally);

		NodeParams<TallyKey,Tally,TallyFielder> params = paramsBuilder.build();

		this.tallyNode = register((MemcachedNode<TallyKey, Tally, TallyFielder>)clientType
				.createNodeWithoutAdapters(params));
	}

	/********************************** config **********************************/

	@Override
	public List<ClientId> getClientIds(){
		return clientIds;
	}

	/*************************** get/set ***********************************/

	public MemcachedNode<TallyKey, Tally, TallyFielder> tally() {
		return tallyNode;
	}
}


