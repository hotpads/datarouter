package com.hotpads.datarouter.client;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hibernate.HibernateClientType;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.concurrent.FutureTool;

/**
 * Clients is a registry or cache of all clients in a DatarouterContext. Clients are expensive to create, so we reuse
 * them for the life of the application. This class also provides a lazy-initialization feature that defers connection
 * creation, authentication, and connection pool warm-up until an application request triggers it.
 * 
 * This class can be used for DatarouterManagement, such as displaying a web page listing all clients.
 * 
 * @author mcorgan
 * 
 */
@Singleton
public class Clients{
	private static Logger logger = LoggerFactory.getLogger(Clients.class);

	protected Set<String> configFilePaths = SetTool.createTreeSet();
	protected Collection<Properties> multiProperties = ListTool.createArrayList();
	protected Map<String, Object> params;
	
	protected NavigableSet<ClientId> clientIds = SetTool.createTreeSet();
	protected List<Client> clients = ListTool.createArrayList();

	protected Map<String,LazyClientProvider> lazyClientInitializerByName = new ConcurrentHashMap<String,LazyClientProvider>();

	public static final ClientType DEFAULT_CLIENT_TYPE = HibernateClientType.INSTANCE;
	
	
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

	@Inject
	public Clients(){
	}
	
	public void registerConfigFile(String configFilePath){
		if(StringTool.notEmpty(configFilePath) && !configFilePaths.contains(configFilePath)){
			configFilePaths.add(configFilePath);
			multiProperties.add(PropertiesTool.parse(configFilePath));
		}
	}
	
	public void registerClientIds(DataRouterContext context, Collection<ClientId> clientIdsToAdd) {
		clientIds.addAll(CollectionTool.nullSafe(clientIdsToAdd));
		for(ClientId clientId : IterableTool.nullSafe(clientIds)) {
			initClientFactoryIfNull(context, clientId.getName());
		}
	}
	
	
	
	/********************************** initialize ******************************/
	
	public void initializeEagerClients(DataRouterContext context){
		final List<String> eagerClientNames = getClientNamesRequiringEagerInitialization();
		getClients(context, eagerClientNames);
	}
	
	public ClientType getClientTypeInstance(String clientName){
		RouterOptions routerOptions = new RouterOptions(multiProperties);
		return routerOptions.getClientTypeInstance(clientName);
	}
	
	private synchronized void initClientFactoryIfNull(DataRouterContext datarouterContext, String clientName) {
		if(lazyClientInitializerByName.containsKey(clientName)) { return; }
		ClientType clientTypeInstance = getClientTypeInstance(clientName);
		List<PhysicalNode<?,?>> physicalNodesForClient = datarouterContext.getNodes().getPhysicalNodesForClient(
				clientName);
		ClientFactory clientFactory = clientTypeInstance.createClientFactory(datarouterContext, clientName, 
				physicalNodesForClient);
		lazyClientInitializerByName.put(clientName, new LazyClientProvider(clientFactory));
	}
	
	
	/******************** getNames **********************************************/
		
	private List<String> getClientNamesRequiringEagerInitialization(){
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

	public NavigableSet<ClientId> getClientIds(){
		return clientIds;
	}

	public List<String> getClientNames(){
		return ClientId.getNames(clientIds);
	}
	
	public Client getClient(String clientName){
		return lazyClientInitializerByName.get(clientName).call();
	}
	
	public List<Client> getClients(DataRouterContext datarouterContext, Collection<String> clientNames){
		List<Client> clients = ListTool.createArrayListWithSize(clientNames);
		List<LazyClientProvider> providers = ListTool.createLinkedList();//TODO don't create until needed
		for(String clientName : CollectionTool.nullSafe(clientNames)){
			LazyClientProvider provider = lazyClientInitializerByName.get(clientName);
			if(provider.isInitialized()){
				clients.add(provider.call());//these can be added immediately (normal code path)
			}else{
				providers.add(provider);//these must be initialized first
			}
		}
		if(CollectionTool.notEmpty(providers)){
			clients.addAll(FutureTool.submitAndGetAll(providers, datarouterContext.getExecutorService()));
		}
		return clients;
	}
	
	public List<Client> getAllClients(DataRouterContext datarouterContext){
		return getClients(datarouterContext, ClientId.getNames(clientIds));
	}
	
	
}

