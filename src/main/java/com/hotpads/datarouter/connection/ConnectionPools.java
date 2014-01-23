package com.hotpads.datarouter.connection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.Clients;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.ThrowableTool;

public class ConnectionPools {
	private static Logger logger = Logger.getLogger(ConnectionPools.class);

	protected NavigableSet<ClientId> clientIds = SetTool.createTreeSet();
	protected Collection<String> configFilePaths = ListTool.createArrayList();
	protected Collection<Properties> multiProperties = ListTool.createArrayList();
	protected Map<String,JdbcConnectionPool> connectionPoolByName = MapTool.createConcurrentHashMap();

	public static final String		
		prefixPool = Clients.prefixClient,//"pool.",
		poolDefault = "default";
	
	/******************************* constructors **********************************/
	
	public ConnectionPools(){
	}
	
	public void registerClientIds(Collection<ClientId> clientIdsToAdd, String configFilePath) {
		Preconditions.checkNotNull(configFilePath);
		clientIds.addAll(CollectionTool.nullSafe(clientIdsToAdd));
		configFilePaths.add(configFilePath);
		multiProperties.add(PropertiesTool.parse(configFilePath));
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
				logger.error(ExceptionTool.getStackTraceAsString(e));
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
			JdbcConnectionPool connectionPool = new JdbcConnectionPool(connectionPoolName, multiProperties, writable);
			connectionPoolByName.put(connectionPool.getName(), connectionPool);
		}catch(Exception e){
			logger.error("error instantiating ConnectionPool:"+connectionPoolName);
			logger.error(ThrowableTool.getStackTraceAsString(e));
		}
	}
}





