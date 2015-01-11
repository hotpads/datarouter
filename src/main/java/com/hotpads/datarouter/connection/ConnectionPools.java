package com.hotpads.datarouter.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.util.ApplicationPaths;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.PropertiesTool;

@Singleton
public class ConnectionPools {
	private static Logger logger = LoggerFactory.getLogger(ConnectionPools.class);

	public static final String		
		prefixPool = Clients.PREFIX_client,//"pool.",
		poolDefault = "default";

	//injected
	private ApplicationPaths applicationPaths;
	
	//not injected
	private NavigableSet<ClientId> clientIds;
	private Set<String> configFilePaths;
	private Collection<Properties> multiProperties;
	private Map<String,JdbcConnectionPool> connectionPoolByName;
	
	
	/******************************* constructors **********************************/
	
	@Inject
	ConnectionPools(ApplicationPaths applicationPaths){
		this.applicationPaths = applicationPaths;

		this.clientIds = new TreeSet<>();
		this.configFilePaths = new TreeSet<>();
		this.multiProperties = new ArrayList<>();
		this.connectionPoolByName = new ConcurrentHashMap<>();
	}
	
	public void registerClientIds(Collection<ClientId> clientIdsToAdd, String configFilePath) {
		Preconditions.checkNotNull(configFilePath);
		clientIds.addAll(CollectionTool.nullSafe(clientIdsToAdd));
		if(!configFilePaths.contains(configFilePath)){
			configFilePaths.add(configFilePath);
			multiProperties.add(PropertiesTool.parse(configFilePath));
		}
	}

	/****************** shutdown ************************************/
	
	public void shutdown(){
		for(JdbcConnectionPool pool : MapTool.nullSafe(this.connectionPoolByName).values()){
			pool.shutdown();
		}
	}

	
	/*************************** getConnectionPools ****************************************/

	public JdbcConnectionPool getConnectionPool(String name){
		if( ! this.connectionPoolByName.containsKey(name)){ 
			try {
				this.initializeConnectionPool(name);
			} catch (Exception e) {//maybe have caller indicate whether to catch this exception?
				logger.error("", e);
			}
		}
		return this.connectionPoolByName.get(name);
	}
	
	public List<JdbcConnectionPool> getConnectionPools(Collection<String> connectionPoolNames){
		List<JdbcConnectionPool> connectionPools = ListTool.createLinkedList();
		for(String connectionPoolName : CollectionTool.nullSafe(connectionPoolNames)){
			connectionPools.add(this.getConnectionPool(connectionPoolName));
		}
		return connectionPools;
	}
	
	public Collection<JdbcConnectionPool> getAllConnectionPools(){
		return connectionPoolByName.values();//won't return uninitialized pools
	}
	
	
	public void initializeConnectionPool(String connectionPoolName){		
		try{
			boolean writable = ClientId.getWritableNames(clientIds).contains(connectionPoolName);
			JdbcConnectionPool connectionPool = new JdbcConnectionPool(applicationPaths, connectionPoolName,
					multiProperties, writable);
			connectionPoolByName.put(connectionPool.getName(), connectionPool);
		}catch(Exception e){
			logger.error("error instantiating ConnectionPool:"+connectionPoolName, e);
		}
	}
}





