package com.hotpads.datarouter.client.imp.redis.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.client.imp.redis.RedisClientType;
import com.hotpads.util.core.concurrent.NamedThreadFactory;

public class RedisClientImp extends BaseClient implements RedisClient{

	protected redis.clients.jedis.Jedis jedisClient;
	protected ExecutorService executorService;

	/** constructor **********************************************************/

	public RedisClientImp(String name, redis.clients.jedis.Jedis jedisClient, ClientAvailabilitySettings
			clientAvailabilitySettings){
		super(name, clientAvailabilitySettings);
		this.jedisClient = jedisClient;
		NamedThreadFactory threadFactory = new NamedThreadFactory(null, "HTablePool", true);
		this.executorService = Executors.newCachedThreadPool(threadFactory);
	}

	/** get/set **************************************************************/

	@Override
	public ExecutorService getExecutorService(){
		return executorService;
	}

	@Override
	public redis.clients.jedis.Jedis getJedisClient(){
		return jedisClient;
	}

	@Override
	public void shutdown(){
		jedisClient.shutdown();
	}

	@Override
	public ClientType getType(){
		return RedisClientType.INSTANCE;
	}
}