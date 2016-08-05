package com.hotpads.datarouter.client.imp.redis.test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.imp.redis.RedisClientType;
import com.hotpads.datarouter.client.imp.redis.node.RedisNode;
import com.hotpads.datarouter.client.imp.redis.test.databean.RedisTestDatabean;
import com.hotpads.datarouter.client.imp.redis.test.databean.RedisTestDatabean.RedisTestDatabeanFielder;
import com.hotpads.datarouter.client.imp.redis.test.databean.RedisTestDatabeanKey;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;

public class RedisTestRouter extends BaseRouter{

	private static final int VERSION_RedisTest = 2;

	private final DatarouterClients datarouterClients;

	/** client names *********************************************************/

	private final List<ClientId> clientIds;

	@Override
	public List<ClientId> getClientIds(){
		return clientIds;
	}

	/** nodes ****************************************************************/

	private final RedisNode<RedisTestDatabeanKey,RedisTestDatabean,RedisTestDatabeanFielder> redisNode;

	/** constructor **********************************************************/

	@Inject
	public RedisTestRouter(Datarouter datarouter, DatarouterClients datarouterClients, ClientId clientId){
		super(datarouter, DrTestConstants.CONFIG_PATH, RedisTestRouter.class.getSimpleName());
		this.datarouterClients = datarouterClients;
		this.clientIds = Arrays.asList(clientId);
		this.redisNode = buildRedisNode(clientId);
	}

	/** get/set **************************************************************/

	public RedisNode<RedisTestDatabeanKey,RedisTestDatabean,RedisTestDatabeanFielder> redisNode() {
		return redisNode;
	}

	/** helper ***************************************************************/

	private RedisNode<RedisTestDatabeanKey,RedisTestDatabean,RedisTestDatabeanFielder>buildRedisNode(ClientId client){
		String clientName = client.getName();
		RedisClientType clientType = (RedisClientType) datarouterClients.getClientTypeInstance(clientName);
		Objects.requireNonNull(clientType, "clientType not found for clientName:" + clientName);

		NodeParamsBuilder<RedisTestDatabeanKey,RedisTestDatabean,RedisTestDatabeanFielder> paramsBuilder =
				new NodeParamsBuilder<RedisTestDatabeanKey ,RedisTestDatabean,
				RedisTestDatabeanFielder>(this,RedisTestDatabean::new)
				.withClientId(client)
				.withFielder(RedisTestDatabeanFielder::new)
				.withSchemaVersion(VERSION_RedisTest);

		NodeParams<RedisTestDatabeanKey,RedisTestDatabean,RedisTestDatabeanFielder> params = paramsBuilder.build();
		return register(clientType.createNodeWithoutAdapters(params));
	}
}