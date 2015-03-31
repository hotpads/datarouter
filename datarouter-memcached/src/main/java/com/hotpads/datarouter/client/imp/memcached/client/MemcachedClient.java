package com.hotpads.datarouter.client.imp.memcached.client;

import java.util.concurrent.ExecutorService;

import com.hotpads.datarouter.client.Client;

public interface MemcachedClient 
extends Client{
	
	ExecutorService getExecutorService();
	net.spy.memcached.MemcachedClient getSpyClient();

}
