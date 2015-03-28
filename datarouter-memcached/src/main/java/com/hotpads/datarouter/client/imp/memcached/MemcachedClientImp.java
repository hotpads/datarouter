package com.hotpads.datarouter.client.imp.memcached;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.util.core.concurrent.NamedThreadFactory;

public class MemcachedClientImp 
extends BaseClient
implements MemcachedClient{

	protected String name;
	protected net.spy.memcached.MemcachedClient spyClient;
	protected ExecutorService executorService;
	
	
	public MemcachedClientImp(String name, net.spy.memcached.MemcachedClient spyClient){
		this.name = name;
		this.spyClient = spyClient;
		NamedThreadFactory threadFactory = new NamedThreadFactory(null, "HTablePool", true);
		this.executorService = Executors.newCachedThreadPool(threadFactory);
	}

	@Override
	public ExecutorService getExecutorService(){
		return executorService;
	}

	@Override
	public net.spy.memcached.MemcachedClient getSpyClient(){
		return spyClient;
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public void shutdown(){
		//should we shutdown the executorService?
		try{
			spyClient.shutdown();
		}catch(MemcachedStateException e){
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public ClientType getType(){
		return MemcachedClientType.INSTANCE;
	}
	
//	net.spy.memcached.MemcachedClient spyClient;
	

	
	
	
}
