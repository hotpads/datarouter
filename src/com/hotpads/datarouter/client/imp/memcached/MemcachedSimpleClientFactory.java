package com.hotpads.datarouter.client.imp.memcached;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.profile.PhaseTimer;


public class MemcachedSimpleClientFactory 
implements MemcachedClientFactory{
	private static Logger logger = Logger.getLogger(MemcachedSimpleClientFactory.class);
	
	protected DataRouterContext drContext;
	protected String clientName;
	protected List<String> configFilePaths;
	protected List<Properties> multiProperties;
	protected ExecutorService executorService;
	protected MemcachedOptions options;
	protected MemcachedClient client;
	
	
	public MemcachedSimpleClientFactory(
			DataRouterContext drContext,
			String clientName, 
			ExecutorService executorService){
		this.drContext = drContext;
		this.clientName = clientName;
		this.executorService = executorService;

		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = PropertiesTool.fromFiles(configFilePaths);
		this.options = new MemcachedOptions(multiProperties, clientName);
	}
	
	
	@Override
	public MemcachedClient getClient(){
		if(client!=null){ return client; }
		CountDownLatch latch = new CountDownLatch(1);
		if(client!=null){ return client; }
		Future<MemcachedClient> future = executorService.submit(new Callable<MemcachedClient>(){
			@Override public MemcachedClient call(){//i forget why this is in a separate thread... timeout-able?
				if(client!=null){ return client; }
				logger.warn("activating Memcached client "+clientName);
				PhaseTimer timer = new PhaseTimer(clientName);
				net.spy.memcached.MemcachedClient spyClient;
				try{
					spyClient = new net.spy.memcached.MemcachedClient(options.getServers());
				}catch(IOException e){
					throw new RuntimeException(e);
				}
				MemcachedClient newClient = new MemcachedClientImp(clientName, spyClient);
				logger.warn(timer.add("done"));
				return newClient;
			}
		});
		try{
			this.client = future.get();
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}catch(ExecutionException e){
			throw new RuntimeException(e);
		}
		latch.countDown();
		return client;
	}
	
	@Override
	public boolean isInitialized(){
		return client != null;
	}
}
