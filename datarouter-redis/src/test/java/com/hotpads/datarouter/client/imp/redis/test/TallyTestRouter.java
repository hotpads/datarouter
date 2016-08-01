package com.hotpads.datarouter.client.imp.redis.test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.imp.redis.RedisClientType;
import com.hotpads.datarouter.client.imp.redis.node.RedisNode;
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

	private final DatarouterClients datarouterClients;

	/*************************** client names ********************************/

	private final List<ClientId> clientIds;

	@Override
	public List<ClientId> getClientIds(){
		return clientIds;
	}

	/********************************** nodes ********************************/

	private final RedisNode<TallyKey,Tally,TallyFielder> tallyNode;

	/******************************* constructor *****************************/

	@Inject
	public TallyTestRouter(Datarouter datarouter, DatarouterClients datarouterClients, NodeFactory nodeFactory,
			ClientId clientId, boolean useFielder){
		super(datarouter, DrTestConstants.CONFIG_PATH, TallyTestRouter.class.getSimpleName());

		this.datarouterClients = datarouterClients;
		this.clientIds = Arrays.asList(clientId);

		this.tallyNode = buildTallyNode(clientId);
	}


	/*************************** get/set *************************************/

	public RedisNode<TallyKey,Tally,TallyFielder> tally() {
		return tallyNode;
	}

	/********************************** helper *******************************/

	private RedisNode<TallyKey, Tally, TallyFielder> buildTallyNode(ClientId clientId){
		String clientName = clientId.getName();
		RedisClientType clientType = (RedisClientType) datarouterClients.getClientTypeInstance(clientName);
		Objects.requireNonNull(clientType, "clientType not found for clientName:" + clientName);
		NodeParamsBuilder<TallyKey, Tally, TallyFielder> paramsBuilder = new NodeParamsBuilder<TallyKey,Tally,
				TallyFielder>(this,Tally::new)
				.withClientId(clientId)
				.withFielder(TallyFielder::new)
				.withSchemaVersion(VERSION_Tally);

		NodeParams<TallyKey,Tally,TallyFielder> params = paramsBuilder.build();
		return register(clientType.createNodeWithoutAdapters(params));
	}
}