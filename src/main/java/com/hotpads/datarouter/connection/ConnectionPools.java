package com.hotpads.datarouter.connection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.util.ApplicationRootPath;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.SetTool;

@Singleton
public class ConnectionPools {
	private static Logger logger = LoggerFactory.getLogger(ConnectionPools.class);

	private ApplicationRootPath applicationRootPath;
	
	private NavigableSet<ClientId> clientIds = SetTool.createTreeSet();
	private Set<String> configFilePaths = SetTool.createTreeSet();
	private Collection<Properties> multiProperties = ListTool.createArrayList();
	private Map<String,JdbcConnectionPool> connectionPoolByName = MapTool.createConcurrentHashMap();

	public static final String		
		prefixPool = Clients.PREFIX_client,//"pool.",
		poolDefault = "default";
	
	/******************************* constructors **********************************/
	
	@Inject
	ConnectionPools(ApplicationRootPath applicationRootPath){
		this.applicationRootPath = applicationRootPath;
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
			JdbcConnectionPool connectionPool = new JdbcConnectionPool(applicationRootPath, connectionPoolName,
					multiProperties, writable);
			connectionPoolByName.put(connectionPool.getName(), connectionPool);
		}catch(Exception e){
			logger.error("error instantiating ConnectionPool:"+connectionPoolName, e);
		}
	}
}





