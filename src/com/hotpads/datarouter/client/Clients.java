package com.hotpads.datarouter.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.DataRouterFactory;
import com.hotpads.datarouter.exception.UnavailableException;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.ThrowableTool;

public class Clients{
	private static Logger logger = Logger.getLogger(Clients.class);

	protected String configFileLocation;
	protected DataRouterFactory<? extends DataRouter> datapus;
	protected Map<String, Object> params;
	
	protected List<String> allClientNames = ListTool.createLinkedList();
	
	protected Map<String,Client> clientByName = new HashMap<String,Client>();
	
	protected Set<String> initializingClients = new HashSet<String>();

	public static final String
		prefixClients = "clients",
		paramForceInitMode = ".forceInitMode",
		paramNames = ".names",
		
		prefixClient = "client.",
		clientDefault = "default",
		paramConnectionPool = ".connectionPool",
		paramInitMode = ".initMode",
		paramSource = ".source",
		paramType = ".type",
		paramSlave = ".slave";
	
	/******************************* constructors **********************************/

	public Clients(String configFileLocation, DataRouterFactory<? extends DataRouter> datapus, Map<String,Object> params)
			throws FileNotFoundException, IOException {

		this.configFileLocation = configFileLocation;
		this.datapus = datapus;
		this.params = params;

		Properties properties = PropertiesTool
				.fromFile(this.configFileLocation);
		this.allClientNames = getAllClientNames(properties);

		this.initializeEagerClients();
	}
	
	
	/******************* add clients **********************************************/
	
	public void add(Client client){
		this.clientByName.put(client.getName(), client);
	}
	
	
	/*************************** getClients 
	 * @throws InterruptedException ****************************************/
	
	long MAX_INIT_WAIT_TIME_MS = 30000;
	
	/*
	 * - Prevent multiple versions of the same client from instantiating (would be a very bad resource leak)
	 * - Allow clients to instantiate in parallel
	 */
	public Client getClient(String name){
		if( ! this.clientByName.containsKey(name)){ 
			boolean initializeVsWait = false;
			synchronized(this.initializingClients){
				if(!this.initializingClients.contains(name)){
					this.initializingClients.add(name);
					initializeVsWait = true;
				}
			}
			if(initializeVsWait){
				try {
					this.initializeClient(name);
				} catch (Exception e) {
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}finally{
					this.initializingClients.remove(name);
				}
			}
			long waitStartTimeMs = System.currentTimeMillis();
			while(true){
				//see if it finished initializing
				if(!this.initializingClients.contains(name)){
					break;
				}
				//have we waited enough for it?
				long elapsedWaitTime = System.currentTimeMillis() - waitStartTimeMs;
				if(elapsedWaitTime > MAX_INIT_WAIT_TIME_MS){
					throw new UnavailableException("Could not acquire client after "+MAX_INIT_WAIT_TIME_MS+"ms");
				}
				//try again in 1 second
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return this.clientByName.get(name);
	}
	
	public List<Client> getClients(Collection<String> clientNames){
		List<Client> clients = ListTool.createLinkedList();
		for(String clientName : CollectionTool.nullSafe(clientNames)){
			clients.add(this.getClient(clientName));
		}
		return clients;
	}
	
	public List<Client> getAllClients(){
		return this.getClients(this.allClientNames);
	}
	
	
	/******************** getNames **********************************************/
	
	public static List<String> getAllClientNames(Properties properties){
		String clientNamesCsv = properties.getProperty(prefixClients+paramNames);
		String[] clientNames = StringTool.isEmpty(clientNamesCsv)?null:clientNamesCsv.split(",");
		return ListTool.createArrayList(clientNames);
	}

	
	public static final String clientInitModeEager = "eager";
	public static final String clientInitModeLazy = "lazy";
		
	public static List<String> getClientNamesRequiringEagerInitialization(Properties properties){
		List<String> clientNames = getAllClientNames(properties);
		
		ClientInitMode forceInitMode = ClientInitMode.fromString(
				properties.getProperty(prefixClients+paramForceInitMode), null);
		
		if(forceInitMode != null){
			if(ClientInitMode.eager.equals(forceInitMode)){
				return clientNames;
			}else{
				return null;
			}
		}
		
		ClientInitMode defaultInitMode = ClientInitMode.fromString(
				properties.getProperty(prefixClient+clientDefault+paramInitMode), ClientInitMode.lazy);
		
		List<String> clientNamesRequiringEagerInitialization = ListTool.createLinkedList();
		for(String name : CollectionTool.nullSafe(clientNames)){
			ClientInitMode mode = ClientInitMode.fromString(
					properties.getProperty(prefixClient+name+paramInitMode), defaultInitMode);
			if(ClientInitMode.eager.equals(mode)){
				clientNamesRequiringEagerInitialization.add(name);
			}
		}
		return clientNamesRequiringEagerInitialization;
	}
	
	
	/********************************** initialize ******************************/
	
	public void initializeEagerClients() throws IOException{	
		Properties properties = PropertiesTool.fromFile(this.configFileLocation);
		List<String> clientNames = getClientNamesRequiringEagerInitialization(properties);
		for(String name : CollectionTool.nullSafe(clientNames)){
			this.initializeClient(name);
		}
	}
	
	
	public void initializeClient(String clientName) throws IOException{	
		Properties properties = PropertiesTool.fromFile(this.configFileLocation);
		try{
			String typeString = properties.getProperty(prefixClient+clientName+paramType);
			ClientType clientType = ClientType.fromString(typeString);
			Client client = clientType.getClientFactory().createClient(datapus, clientName, properties, params);
			this.add(client);
			logger.info("added client("+clientType.toString()+"):"+client.getName());
		}catch(Exception e){
			logger.error("error instantiating client:"+clientName);
			logger.error(ThrowableTool.getStackTraceAsString(e));
		}
	}
	
}
