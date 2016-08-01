package com.hotpads.datarouter.client.imp.redis.client;

import java.util.concurrent.ExecutorService;

import com.hotpads.datarouter.client.Client;

public interface RedisClient extends Client{

	ExecutorService getExecutorService();
	redis.clients.jedis.Jedis getJedisClient();
}