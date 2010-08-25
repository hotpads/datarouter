package com.hotpads.datarouter.client.imp.memcached;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientType;

public class MemcachedClient implements Client{

	String name;
	public String getName(){
		return name;
	}
	
	@Override
	public ClientType getType(){
		return ClientType.memcached;
	}
	
//	net.spy.memcached.MemcachedClient spyClient;
	

	
	
	
}
