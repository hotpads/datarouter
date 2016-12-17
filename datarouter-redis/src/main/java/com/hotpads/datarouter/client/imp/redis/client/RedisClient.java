package com.hotpads.datarouter.client.imp.redis.client;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.client.imp.redis.RedisClientType;

import redis.clients.jedis.Jedis;

public class RedisClient extends BaseClient{

	private final Jedis jedisClient;
	private final RedisClientType redisClientType;

	/** constructor **********************************************************/

	public RedisClient(String name, Jedis jedisClient, ClientAvailabilitySettings clientAvailabilitySettings,
			RedisClientType redisClientType){
		super(name, clientAvailabilitySettings);
		this.jedisClient = jedisClient;
		this.redisClientType = redisClientType;
	}

	/** get/set **************************************************************/

	public Jedis getJedisClient(){
		return jedisClient;
	}

	@Override
	public void shutdown(){
		jedisClient.close();
	}

	@Override
	public ClientType getType(){
		return redisClientType;
	}
}