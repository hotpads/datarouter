package com.hotpads.datarouter.connection;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.ThrowableTool;

public class ConnectionPools {
	private static Logger logger = Logger.getLogger(ConnectionPools.class);

	protected DataRouter router;
	protected String configFileLocation;
	protected Properties properties;
	protected Map<String,JdbcConnectionPool> connectionPoolByName = MapTool.createConcurrentHashMap();

	public static final String		
		prefixPool = Clients.prefixClient,//"pool.",
		poolDefault = "default";
	
	/******************************* constructors **********************************/
	
	public ConnectionPools(DataRouter router) throws IOException {
		this.router = router;
		this.configFileLocation = router.getConfigLocation();
		this.properties = PropertiesTool.nullSafeFromFile(this.configFileLocation);
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
			} catch (IOException e) {
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
	
	
	public void initializeConnectionPool(String connectionPoolName) throws IOException{		
		try{
			boolean writable = ClientId.getWritableNames(router.getClientIds()).contains(connectionPoolName);
			JdbcConnectionPool connectionPool = new JdbcConnectionPool(connectionPoolName, properties, writable);
			connectionPoolByName.put(connectionPool.getName(), connectionPool);
		}catch(Exception e){
			logger.error("error instantiating ConnectionPool:"+connectionPoolName);
			logger.error(ThrowableTool.getStackTraceAsString(e));
		}
	}
}





