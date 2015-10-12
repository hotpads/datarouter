package com.hotpads.datarouter.client.imp.memcached.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.util.core.profile.PhaseTimer;

import net.spy.memcached.KetamaConnectionFactory;


public class MemcachedSimpleClientFactory
implements ClientFactory{
	private static Logger logger = LoggerFactory.getLogger(MemcachedSimpleClientFactory.class);

	private String clientName;
	private Set<String> configFilePaths;
	private List<Properties> multiProperties;
	private MemcachedOptions options;
	private final ClientAvailabilitySettings clientAvailabilitySettings;

	public MemcachedSimpleClientFactory(Datarouter datarouter, String clientName, ClientAvailabilitySettings
			clientAvailabilitySettings){
		this.clientName = clientName;
		this.clientAvailabilitySettings = clientAvailabilitySettings;
		this.configFilePaths = datarouter.getConfigFilePaths();
		this.multiProperties = DrPropertiesTool.fromFiles(configFilePaths);
		this.options = new MemcachedOptions(multiProperties, clientName);
	}

	@Override
	public Client call(){
		logger.info("activating Memcached client "+clientName);
		PhaseTimer timer = new PhaseTimer(clientName);
		net.spy.memcached.MemcachedClient spyClient;
		try{
			//use KetamaConnectionFactory for consistent hashing between memcached servers
			spyClient = new net.spy.memcached.MemcachedClient(new KetamaConnectionFactory(), Arrays.asList(
					options.getServers()));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		MemcachedClient newClient = new MemcachedClientImp(clientName, spyClient, clientAvailabilitySettings);
		logger.warn(timer.add("done").toString());
		return newClient;
	}
}
