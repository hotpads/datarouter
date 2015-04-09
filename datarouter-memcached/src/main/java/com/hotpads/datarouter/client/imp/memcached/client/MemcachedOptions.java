package com.hotpads.datarouter.client.imp.memcached.client;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.hotpads.util.core.properties.TypedProperties;

public class MemcachedOptions extends TypedProperties{
	
	protected String clientPrefix;

	public MemcachedOptions(List<Properties> multiProperties, String clientName){
		super(multiProperties);
		this.clientPrefix = "client."+clientName+".memcached.";
	}
	
	public Integer getNumServers(){
		return getInteger(clientPrefix+"numServers");
	}
	
	public InetSocketAddress[] getServers(){
		List<InetSocketAddress> servers = new LinkedList<>();
		for(int i=0; i < getNumServers(); ++i){
			String key = clientPrefix+"server."+i;
			String hostNameAndPort = getString(key);
			String[] hostnameAndPortTokens = hostNameAndPort.split(":");
			String hostname = hostnameAndPortTokens[0];
			int port = Integer.valueOf(hostnameAndPortTokens[1]);
			servers.add(new InetSocketAddress(hostname, port));
		}
		return servers.toArray(new InetSocketAddress[servers.size()]);
	}
	
}
