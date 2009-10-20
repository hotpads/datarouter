package com.hotpads.datarouter.connection;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.DataRouterFactory;
import com.hotpads.datarouter.client.ClientInitMode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.ThrowableTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class ConnectionPools {
	private static Logger logger = Logger.getLogger(ConnectionPools.class);

	protected String configFileLocation;
	protected DataRouterFactory<? extends DataRouter> datapus;
	protected Map<String, Object> params;
	
	protected List<String> allConnectionPoolNames = ListTool.createLinkedList();

	protected Map<String,JdbcConnectionPool> connectionPoolByName = new HashMap<String,JdbcConnectionPool>();

	
	public static final String
		prefixConnectionPools = "connectionPools",
		paramForceInitMode = ".forceInitMode",
		paramNames = ".names",
		
		prefixConnectionPool = "connectionPool.",
		connectionPoolDefault = "default",
		paramInitMode = ".initMode",
		paramSource = ".source";
	
	/******************************* constructors **********************************/
	
	public ConnectionPools(String configFileLocation, DataRouterFactory<? extends DataRouter> datapus, Map<String, Object> params) 
	throws FileNotFoundException, IOException {
	
		this.configFileLocation = configFileLocation;
		this.datapus = datapus;
		this.params = params;
		
		Properties properties = PropertiesTool.fromFile(this.configFileLocation);
		this.allConnectionPoolNames = getAllConnectionPoolNames(properties);
		
		this.initializeEagerConnectionPools();
	}

	/****************** shutdown ************************************/
	
	public void shutdown(){
		for(JdbcConnectionPool pool : MapTool.nullSafe(this.connectionPoolByName).values()){
			pool.shutdown();
		}
	}
	
	
	/******************* add pools **********************************************/

	public void add(JdbcConnectionPool connectionPool){
		this.connectionPoolByName.put(connectionPool.getName(), connectionPool);
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
	
	public List<JdbcConnectionPool> getAllConnectionPools(){
		return this.getConnectionPools(this.allConnectionPoolNames);
	}
	
	
	/******************** getNames **********************************************/
	
	public List<String> getExistingConnectionPoolNames(){
		return this.allConnectionPoolNames;
	}
	
	public static List<String> getAllConnectionPoolNames(Properties properties){
		String connectionPoolNamesCsv = properties.getProperty(prefixConnectionPools+paramNames);
		String[] connectionPoolNames = StringTool.isEmpty(connectionPoolNamesCsv)?null:connectionPoolNamesCsv.split(",");
		return ListTool.createArrayList(connectionPoolNames);
	}
		
	public static List<String> getConnectionPoolNamesRequiringEagerInitialization(Properties properties){
		List<String> connectionPoolNames = getAllConnectionPoolNames(properties);
		
		ClientInitMode forceInitMode = ClientInitMode.fromString(
				properties.getProperty(prefixConnectionPool+paramForceInitMode), null);
		
		if(forceInitMode != null){
			if(ClientInitMode.eager.equals(forceInitMode)){
				return connectionPoolNames;
			}else{
				return null;
			}
		}
		
		ClientInitMode defaultInitMode = ClientInitMode.fromString(
				properties.getProperty(prefixConnectionPool+connectionPoolDefault+paramInitMode), ClientInitMode.lazy);
		
		List<String> connectionPoolNamesRequiringEagerInitialization = ListTool.createLinkedList();
		for(String name : CollectionTool.nullSafe(connectionPoolNames)){
			ClientInitMode mode = ClientInitMode.fromString(
					properties.getProperty(prefixConnectionPool+name+paramInitMode), defaultInitMode);
			if(ClientInitMode.eager.equals(mode)){
				connectionPoolNamesRequiringEagerInitialization.add(name);
			}
		}
		return connectionPoolNamesRequiringEagerInitialization;
	}
	

	/********************************** initialize ******************************/
	
	public void initializeEagerConnectionPools() throws IOException{	
		Properties properties = PropertiesTool.fromFile(this.configFileLocation);
		List<String> connectionPoolNames = getConnectionPoolNamesRequiringEagerInitialization(properties);
		for(String name : CollectionTool.nullSafe(connectionPoolNames)){
			this.initializeConnectionPool(name);
		}
	}
	
	
	public void initializeConnectionPool(String connectionPoolName) throws IOException{	
		PhaseTimer timer = new PhaseTimer("initializeConnectionPool:"+connectionPoolName);
		
		Properties properties = PropertiesTool.fromFile(this.configFileLocation);
		
		try{
			JdbcConnectionPool connectionPool = new JdbcConnectionPool(connectionPoolName, properties, params);
			timer.add("initialized");
			
			this.add(connectionPool);
			logger.info(timer);
		}catch(Exception e){
			logger.error("error instantiating ConnectionPool:"+connectionPoolName);
			logger.error(ThrowableTool.getStackTraceAsString(e));
		}
	}
}
