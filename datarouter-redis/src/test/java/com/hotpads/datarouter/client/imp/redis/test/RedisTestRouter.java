package com.hotpads.datarouter.client.imp.redis.test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.imp.redis.RedisClientType;
import com.hotpads.datarouter.client.imp.redis.databean.RedisDatabean;
import com.hotpads.datarouter.client.imp.redis.databean.RedisDatabean.RedisDatabeanFielder;
import com.hotpads.datarouter.client.imp.redis.databean.RedisDatabeanKey;
import com.hotpads.datarouter.client.imp.redis.node.RedisNode;
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

	private final RedisNode<RedisDatabeanKey,RedisDatabean,RedisDatabeanFielder> redisNode;

	/** constructor **********************************************************/

	@Inject
	public RedisTestRouter(Datarouter datarouter, DatarouterClients datarouterClients, ClientId clientId){
		super(datarouter, DrTestConstants.CONFIG_PATH, RedisTestRouter.class.getSimpleName(), null, null);
		this.datarouterClients = datarouterClients;
		this.clientIds = Arrays.asList(clientId);
		this.redisNode = buildRedisNode(clientId);
	}

	/** get/set **************************************************************/

	public RedisNode<RedisDatabeanKey,RedisDatabean,RedisDatabeanFielder> redisNode() {
		return redisNode;
	}

	/** helper ***************************************************************/

	private RedisNode<RedisDatabeanKey,RedisDatabean,RedisDatabeanFielder>buildRedisNode(ClientId client){
		String clientName = client.getName();
		RedisClientType clientType = (RedisClientType) datarouterClients.getClientTypeInstance(clientName);
		Objects.requireNonNull(clientType, "clientType not found for clientName:" + clientName);

		NodeParams<RedisDatabeanKey,RedisDatabean,RedisDatabeanFielder> params =
				new NodeParamsBuilder<>(this,RedisDatabean::new, RedisDatabeanFielder::new)
				.withClientId(client)
				.withSchemaVersion(VERSION_RedisTest)
				.build();

		return register(clientType.createNodeWithoutAdapters(params));
	}
}