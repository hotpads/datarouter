package com.hotpads.datarouter.client.imp.memcached;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.BaseClient;

public class MemcachedClient 
extends BaseClient{

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
