package com.hotpads.datarouter.client;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.connection.ConnectionPools;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.ThrowableTool;
import com.hotpads.util.core.concurrent.NamedThreadFactory;

public class Clients{
	private static Logger logger = Logger.getLogger(Clients.class);

	protected String configFileLocation;
	protected Properties properties;
	protected DataRouter router;
	protected Map<String, Object> params;
	
	protected List<String> allClientNames = ListTool.createLinkedList();

	protected Map<String,ClientFactory> clientFactoryByName = new ConcurrentHashMap<String,ClientFactory>();

	public static final ClientType DEFAULT_CLIENT_TYPE = ClientType.hibernate;
	
	protected ThreadGroup parentThreadGroup;
	protected ThreadFactory threadFactory;
	protected ThreadPoolExecutor executorService;//for async client init and monitoring
	
	public static final String
		prefixClients = "clients",
		paramForceInitMode = ".forceInitMode",
		paramNames = ".names",
		
		prefixClient = "client.",
		clientDefault = "default",
		paramConnectionPool = ".connectionPool",
		paramInitMode = ".initMode",
		paramType = ".type",
		paramSlave = ".slave";
	
	/******************************* constructors **********************************/

	public Clients(ThreadGroup parentThreadGroup, String configFileLocation, DataRouter router) throws IOException {
		this.configFileLocation = configFileLocation;
		this.router = router;
		this.properties = PropertiesTool.nullSafeFromFile(this.configFileLocation);
		this.parentThreadGroup = parentThreadGroup;//new ThreadGroup("DataRouter-"+router.getName());
		this.threadFactory = new NamedThreadFactory(parentThreadGroup, "DataRouter-"+router.getName()+"-clients", true);
		this.executorService = new ThreadPoolExecutor(
				0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
	            new SynchronousQueue<Runnable>(), threadFactory);
		initializeClients();
	}
	
	
	/******************** getNames **********************************************/
		
	public List<String> getClientNamesRequiringEagerInitialization(Properties properties){
		
		ClientInitMode forceInitMode = ClientInitMode.fromString(
				properties.getProperty(prefixClients+paramForceInitMode), null);
		
		if(forceInitMode != null){
			if(ClientInitMode.eager.equals(forceInitMode)){
				return router.getClientNames();
			}else{
				return null;
			}
		}
		
		ClientInitMode defaultInitMode = ClientInitMode.fromString(
				properties.getProperty(prefixClient+clientDefault+paramInitMode), ClientInitMode.lazy);
		
		List<String> clientNamesRequiringEagerInitialization = ListTool.createLinkedList();
		for(String name : CollectionTool.nullSafe(router.getClientNames())){
			ClientInitMode mode = ClientInitMode.fromString(
					properties.getProperty(prefixClient+name+paramInitMode), defaultInitMode);
			if(ClientInitMode.eager.equals(mode)){
				clientNamesRequiringEagerInitialization.add(name);
			}
		}
		return clientNamesRequiringEagerInitialization;
	}
	
	
	/********************************** initialize ******************************/
	
	
	public void initializeClients() throws IOException{	
		String defaultTypeString = properties.getProperty(prefixClient+clientDefault+paramType);
		if(StringTool.isEmpty(defaultTypeString)){ defaultTypeString = DEFAULT_CLIENT_TYPE.toString(); }
		allClientNames = router.getClientNames();
		if(CollectionTool.isEmpty(allClientNames)){ return; }
		final List<String> eagerClientNames = getClientNamesRequiringEagerInitialization(properties);
		for(final String clientName : CollectionTool.nullSafe(allClientNames)){
			ExecutorService exec = Executors.newSingleThreadExecutor(
					new NamedThreadFactory(parentThreadGroup, "Client-"+clientName, true));
			String typeString = properties.getProperty(prefixClient+clientName+paramType);
			if(StringTool.isEmpty(typeString)){ typeString = defaultTypeString; }
			ClientType clientType = ClientType.fromString(typeString);
			ClientFactory clientFactory = clientType.createClientFactory(
					router, clientName, configFileLocation, exec);
			clientFactoryByName.put(clientName, clientFactory);
			boolean eager = CollectionTool.contains(eagerClientNames, clientName);
			if(!eager){
//				logger.warn("registered:"+clientName+" ("+clientType.toString()+")");
			}
		}
		for(final String clientName : CollectionTool.nullSafe(eagerClientNames)){
			executorService.submit(new Callable<Void>(){
				public Void call(){
					try{
						clientFactoryByName.get(clientName).getClient();
					}catch(Exception e){
						logger.error("error activating client:"+clientName);
						logger.error(ThrowableTool.getStackTraceAsString(e));
					}
					return null;
				}
			});
//			executorService.setThreadFactory(threadFactory);//set it back to the default name
		}
		//TODO handle problems
//		executor.shutdown();//i don't think this call blocks.  the invokeAll call does blcok
	}
	
	
	/********************************** access connection pools ******************************/
	
	public ConnectionPools getConnectionPools(){
		return this.router.getConnectionPools();
	}
	
	public List<Client> getClients(Collection<String> clientNames){
		List<Client> clients = ListTool.createLinkedList();
		for(String clientName : CollectionTool.nullSafe(clientNames)){
			clients.add(clientFactoryByName.get(clientName).getClient());
		}
		return clients;
	}
	
	public List<Client> getAllClients(){
		return getClients(allClientNames);
	}
	
	public List<Client> getAllInstantiatedClients(){
		List<Client> clients = ListTool.createLinkedList();
		for(String clientName : CollectionTool.nullSafe(allClientNames)){
			if(clientFactoryByName.get(clientName).isInitialized()){
				clients.add(clientFactoryByName.get(clientName).getClient());
			}
		}
		return clients;
	}
	
	public Client getClient(String clientName){
		ClientFactory clientFactory = clientFactoryByName.get(clientName);
		return clientFactory==null?null:clientFactory.getClient();
	}
}













