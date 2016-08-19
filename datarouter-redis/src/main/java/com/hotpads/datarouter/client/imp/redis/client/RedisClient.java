package com.hotpads.datarouter.client.imp.redis.client;

import java.util.concurrent.ExecutorService;

import com.hotpads.datarouter.client.Client;

import redis.clients.jedis.Jedis;

public interface RedisClient extends Client{

	ExecutorService getExecutorService();
	Jedis getJedisClient();
}