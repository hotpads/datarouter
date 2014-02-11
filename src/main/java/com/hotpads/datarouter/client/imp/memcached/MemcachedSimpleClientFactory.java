package com.hotpads.datarouter.client.imp.memcached;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.profile.PhaseTimer;


public class MemcachedSimpleClientFactory 
implements ClientFactory{
	private static Logger logger = Logger.getLogger(MemcachedSimpleClientFactory.class);
	
	private String clientName;
	private Set<String> configFilePaths;
	private List<Properties> multiProperties;
	private MemcachedOptions options;
	
	
	public MemcachedSimpleClientFactory(DataRouterContext drContext, String clientName){
		this.clientName = clientName;
		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = PropertiesTool.fromFiles(configFilePaths);
		this.options = new MemcachedOptions(multiProperties, clientName);
	}
	
	@Override
	public Client call(){
		logger.info("activating Memcached client "+clientName);
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
}
