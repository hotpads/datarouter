package com.hotpads.datarouter.client;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.connection.ConnectionPools;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.ThrowableTool;

public class Clients{
	private static Logger logger = Logger.getLogger(Clients.class);

	protected DataRouterContext drContext;
	
	protected Collection<String> configFilePaths = ListTool.createArrayList();
	protected Collection<Properties> multiProperties = ListTool.createArrayList();
	protected Map<String, Object> params;
	
	protected NavigableSet<ClientId> clientIds = SetTool.createTreeSet();
	protected List<Client> clients = ListTool.createArrayList();

	protected Map<String,ClientFactory> clientFactoryByName = new ConcurrentHashMap<String,ClientFactory>();

	public static final ClientType DEFAULT_CLIENT_TYPE = ClientType.hibernate;
	
	
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

	public Clients(DataRouterContext drContext){
		this.drContext = drContext;
		initializeEagerClients();
	}
	
	public void registerClientIds(Collection<ClientId> clientIdsToAdd, String configFilePath) {
		clientIds.addAll(CollectionTool.nullSafe(clientIdsToAdd));
		configFilePaths.add(configFilePath);
		multiProperties.add(PropertiesTool.ioAndNullSafeFromFile(configFilePath));
	}
	
	
	
	/********************************** initialize ******************************/
	
	public void initializeEagerClients(){	
		if(CollectionTool.isEmpty(clientIds)){ 
			logger.warn("activate() called on Clients.java with no ClientIds");
			return; 
		}
		final List<String> eagerClientNames = getClientNamesRequiringEagerInitialization();
		for(final ClientId clientId : CollectionTool.nullSafe(clientIds)){
			String clientName = clientId.getName();
//			ExecutorService exec = Executors.newSingleThreadExecutor(
//					new NamedThreadFactory(drContext.getParentThreadGroup(), "Client-"+name, true));
//			String typeString = PropertiesTool.getFirstOccurrence(multiProperties, prefixClient+name+paramType);
//			if(StringTool.isEmpty(typeString)){ typeString = defaultTypeString; }
//			ClientType clientType = ClientType.fromString(typeString);
//			ClientFactory clientFactory = clientType.createClientFactory(drContext,
//					name, drContext.getNodes().getPhysicalNodesForClient(name),
//					drContext.getExecutorService());
//			clientFactoryByName.put(name, clientFactory);
			initClientFactory(clientName);
			boolean eager = CollectionTool.contains(eagerClientNames, clientName);
			if(!eager){
//				logger.warn("registered:"+clientName+" ("+clientType.toString()+")");
			}
		}
		for(final String clientName : CollectionTool.nullSafe(eagerClientNames)){
			drContext.getExecutorService().submit(new Callable<Void>(){
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
	
	protected void initClientFactory(String clientName) {
		String defaultTypeString = PropertiesTool.getFirstOccurrence(multiProperties, 
				prefixClient+clientDefault+paramType);
		if(StringTool.isEmpty(defaultTypeString)){ defaultTypeString = DEFAULT_CLIENT_TYPE.toString(); }
		String typeString = PropertiesTool.getFirstOccurrence(multiProperties, prefixClient+clientName+paramType);
		if(StringTool.isEmpty(typeString)){ typeString = defaultTypeString; }
		ClientType clientType = ClientType.fromString(typeString);
		ClientFactory clientFactory = clientType.createClientFactory(drContext,
				clientName, drContext.getNodes().getPhysicalNodesForClient(clientName),
				drContext.getExecutorService());
		clientFactoryByName.put(clientName, clientFactory);
	}
	
	
	/******************** getNames **********************************************/
		
	protected List<String> getClientNamesRequiringEagerInitialization(){
		
		ClientInitMode forceInitMode = ClientInitMode.fromString(
				PropertiesTool.getFirstOccurrence(multiProperties, prefixClients+paramForceInitMode), null);
		
		if(forceInitMode != null){
			if(ClientInitMode.eager.equals(forceInitMode)){
				return getClientNames();
			}else{
				return null;
			}
		}
		
		ClientInitMode defaultInitMode = ClientInitMode.fromString(PropertiesTool.getFirstOccurrence(
				multiProperties, prefixClient+clientDefault+paramInitMode), ClientInitMode.lazy);
		
		List<String> clientNamesRequiringEagerInitialization = ListTool.createLinkedList();
		for(String name : CollectionTool.nullSafe(getClientNames())){
			ClientInitMode mode = ClientInitMode.fromString(PropertiesTool.getFirstOccurrence(multiProperties,
					prefixClient+name+paramInitMode), defaultInitMode);
			if(ClientInitMode.eager.equals(mode)){
				clientNamesRequiringEagerInitialization.add(name);
			}
		}
		return clientNamesRequiringEagerInitialization;
	}
	
	
	/********************************** access connection pools ******************************/
	
	public ConnectionPools getConnectionPools(){
		return drContext.getConnectionPools();
	}

	public NavigableSet<ClientId> getClientIds(){
		return clientIds;
	}

	public List<String> getClientNames(){
		return ClientId.getNames(clientIds);
	}
	
	public Client getClient(String clientName){
		ClientFactory clientFactory = clientFactoryByName.get(clientName);
		if(clientFactory!=null) { return clientFactory.getClient(); }
		if(!getClientNames().contains(clientName)) { 
			throw new IllegalArgumentException("unknown clientName:"+clientName); 
		}
		initClientFactory(clientName);
		clientFactory = clientFactoryByName.get(clientName);
		return clientFactory.getClient();
	}
	
	public List<Client> getClients(Collection<String> clientNames){
		List<Client> clients = ListTool.createLinkedList();
		for(String clientName : CollectionTool.nullSafe(clientNames)){
			clients.add(clientFactoryByName.get(clientName).getClient());
		}
		return clients;
	}
	
	public List<Client> getAllClients(){
		return getClients(ClientId.getNames(clientIds));
	}
	
	public List<Client> getAllInstantiatedClients(){
		List<Client> clients = ListTool.createLinkedList();
		for(ClientId clientId : CollectionTool.nullSafe(clientIds)){
			String name = clientId.getName();
			if(clientFactoryByName.get(name).isInitialized()){
				clients.add(clientFactoryByName.get(name).getClient());
			}
		}
		return clients;
	}
	
	
}













