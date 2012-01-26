package com.hotpads.datarouter.client.imp.memcached;

import java.util.concurrent.ExecutorService;

import com.hotpads.datarouter.client.Client;

public interface MemcachedClient 
extends Client{

	public static final Long DEFAULT_TIMEOUT_MS = 10 * 1000L;
	
	ExecutorService getExecutorService();
	net.spy.memcached.MemcachedClient getSpyClient();

}
