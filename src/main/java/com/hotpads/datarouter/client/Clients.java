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
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.concurrent.FutureTool;

public class Clients{
	private static Logger logger = Logger.getLogger(Clients.class);

	protected DataRouterContext drContext;
	
	protected Collection<String> configFilePaths = ListTool.createArrayList();
	protected Collection<Properties> multiProperties = ListTool.createArrayList();
	protected Map<String, Object> params;
	
	protected NavigableSet<ClientId> clientIds = SetTool.createTreeSet();
	protected List<Client> clients = ListTool.createArrayList();

	protected Map<String,LazyClientInitializer> lazyClientInitializerByName = new ConcurrentHashMap<String,LazyClientInitializer>();

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
//		initializeEagerClients();//i don't think this will do anything here because clients haven't been registered yet
	}
	
	public void registerClientIds(Collection<ClientId> clientIdsToAdd, String configFilePath) {
		clientIds.addAll(CollectionTool.nullSafe(clientIdsToAdd));
		if(StringTool.notEmpty(configFilePath)){
			configFilePaths.add(configFilePath);
			multiProperties.add(PropertiesTool.parse(configFilePath));
		}
		for(ClientId clientId : IterableTool.nullSafe(clientIds)) {
			initClientFactoryIfNull(clientId.getName());
		}
	}
	
	
	
	/********************************** initialize ******************************/
	
	public void initializeEagerClients(){
		final List<String> eagerClientNames = getClientNamesRequiringEagerInitialization();
		getClients(eagerClientNames);
	}
	
	protected synchronized void initClientFactoryIfNull(String clientName) {
		if(lazyClientInitializerByName.containsKey(clientName)) { return; }
		RouterOptions routerOptions = new RouterOptions(multiProperties);
		DClientType clientTypeInstance = routerOptions.getClientTypeInstance(clientName);
		List<PhysicalNode<?,?>> physicalNodesForClient = drContext.getNodes().getPhysicalNodesForClient(clientName);
		ClientFactory clientFactory = clientTypeInstance.createClientFactory(drContext, clientName, physicalNodesForClient);
		lazyClientInitializerByName.put(clientName, new LazyClientInitializer(clientFactory));
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
		return lazyClientInitializerByName.get(clientName).call();
	}
	
	public List<Client> getClients(Collection<String> clientNames){
		List<Callable<Client>> clientInitializers = ListTool.createArrayList();
		for(String clientName : CollectionTool.nullSafe(clientNames)){
			clientInitializers.add(lazyClientInitializerByName.get(clientName));
		}
		return FutureTool.submitAndGetAll(clientInitializers, drContext.getExecutorService());
	}
	
	public List<Client> getAllClients(){
		return getClients(ClientId.getNames(clientIds));
	}
	
	
}













