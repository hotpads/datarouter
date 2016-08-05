package com.hotpads.datarouter.client.imp.redis.client;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.hotpads.util.core.properties.TypedProperties;

import redis.clients.jedis.HostAndPort;

public class RedisOptions extends TypedProperties{

	protected String clientPrefix;

	public RedisOptions(List<Properties> multiProperties, String clientName){
		super(multiProperties);
		this.clientPrefix = "client." + clientName + ".redis.";
	}

	public Integer getNumServers(){
		return getInteger(clientPrefix + "numServers");
	}

	// Used for JedisClient
	public InetSocketAddress[] getServers(){
		List<InetSocketAddress> servers = new LinkedList<>();
		for(int i = 0; i < getNumServers(); ++i){
			String key = clientPrefix + "server." + i;
			String hostNameAndPort = getString(key);
			String[] hostnameAndPortTokens = hostNameAndPort.split(":");
			String hostname = hostnameAndPortTokens[0];
			int port = Integer.valueOf(hostnameAndPortTokens[1]);
			servers.add(new InetSocketAddress(hostname, port));
		}
		return servers.toArray(new InetSocketAddress[servers.size()]);
	}

	// Used for JedisCluster
	public Set<HostAndPort> getHostsAndPorts(){
		Set<HostAndPort> cluster = new HashSet<>();
		for(int i = 0; i < getNumServers(); ++i){
			String key = clientPrefix + "server." + i;
			String hostNameAndPort = getString(key);
			String[] hostnameAndPortTokens = hostNameAndPort.split(":");
			String hostname = hostnameAndPortTokens[0];
			int port = Integer.valueOf(hostnameAndPortTokens[1]);
			cluster.add(new HostAndPort(hostname, port));
		}
		return cluster;
	}
}