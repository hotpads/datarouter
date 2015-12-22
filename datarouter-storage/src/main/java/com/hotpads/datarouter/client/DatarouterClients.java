package com.hotpads.datarouter.client;

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

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.concurrent.FutureTool;

/**
 * Clients is a registry or cache of all clients in a Datarouter. Clients are expensive to create, so we reuse them for
 * the life of the application. The Clients class also provides a lazy-initialization feature that defers connection
 * creation, authentication, and connection pool warm-up until an application request triggers it.
 *
 * This class can be used for Datarouter management, such as displaying a web page listing all clients.
 */
@Singleton
public class DatarouterClients{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterClients.class);

	public static final String
		CLIENT_default = "default",
		PREFIX_clients = "clients",
		PREFIX_client = "client.",
		PARAM_forceInitMode = ".forceInitMode",
		PARAM_names = ".names",
		PARAM_connectionPool = ".connectionPool",
		PARAM_initMode = ".initMode",
		PARAM_type = ".type",
		PARAM_slave = ".slave"
		;

	//injected
	private final DatarouterInjector injector;

	//not injected
	private final Set<String> configFilePaths;
	private final Collection<Properties> multiProperties;
	private final NavigableSet<ClientId> clientIds;
	private final Map<String,LazyClientProvider> lazyClientProviderByName;

	private RouterOptions routerOptions;


	/******************************* constructors **********************************/

	@Inject
	public DatarouterClients(DatarouterInjector injector){
		this.injector = injector;
		this.configFilePaths = new TreeSet<>();
		this.multiProperties = new ArrayList<>();
		this.clientIds = new TreeSet<>();
		this.lazyClientProviderByName = new ConcurrentHashMap<>();
		this.routerOptions = new RouterOptions(multiProperties);
	}

	public void registerConfigFile(String configFilePath){
		if(DrStringTool.notEmpty(configFilePath) && !configFilePaths.contains(configFilePath)){
			configFilePaths.add(configFilePath);
			multiProperties.add(DrPropertiesTool.parse(configFilePath));
			routerOptions = new RouterOptions(multiProperties);
		}
	}

	public void registerClientIds(Datarouter context, Collection<ClientId> clientIdsToAdd) {
		clientIds.addAll(DrCollectionTool.nullSafe(clientIdsToAdd));
		for(ClientId clientId : DrIterableTool.nullSafe(clientIds)){
			initClientFactoryIfNull(context, clientId.getName());
		}
	}



	/********************************** initialize ******************************/

	public void initializeEagerClients(Datarouter context){
		final List<String> eagerClientNames = getClientNamesRequiringEagerInitialization();
		getClients(context, eagerClientNames);
	}

	public ClientType getClientTypeInstance(String clientName){
		Class<? extends ClientType> clientTypeClass = routerOptions.getClientType(clientName);
		ClientType clientType = injector.getInstance(clientTypeClass);
		return clientType;
	}

	public boolean getDisableable(String clientName){
		return routerOptions.getDisableable(clientName);
	}

	private synchronized void initClientFactoryIfNull(Datarouter context, String clientName) {
		if(lazyClientProviderByName.containsKey(clientName)){
			return;
		}
		ClientType clientTypeInstance = getClientTypeInstance(clientName);
		List<PhysicalNode<?,?>> physicalNodesForClient = new ArrayList<>(context.getNodes().getPhysicalNodesForClient(
				clientName));
		ClientFactory clientFactory = clientTypeInstance.createClientFactory(context, clientName,
				physicalNodesForClient);
		lazyClientProviderByName.put(clientName, new LazyClientProvider(clientFactory));
	}


	/******************** shutdown ********************************************/

	//TODO shutdown clients in parallel
	public void shutdown(){
		for(LazyClientProvider lazyClientProvider : lazyClientProviderByName.values()){
			if( ! lazyClientProvider.isInitialized()){
				continue;
			}
			Client client = lazyClientProvider.call();
			try{
				client.shutdown();
			}catch(Exception e){
				logger.warn("swallowing exception while shutting down client "+client.getName(), e);
			}
		}
	}


	/******************** getNames **********************************************/

	private List<String> getClientNamesRequiringEagerInitialization(){
		String forceInitModeString = DrPropertiesTool.getFirstOccurrence(multiProperties, PREFIX_clients
				+ PARAM_forceInitMode);
		ClientInitMode forceInitMode = ClientInitMode.fromString(forceInitModeString, null);
		if(forceInitMode != null){
			if(ClientInitMode.eager == forceInitMode){
				return getClientNames();
			}
			return null;
		}

		String defaultInitModeString = DrPropertiesTool.getFirstOccurrence(multiProperties, PREFIX_client
				+ CLIENT_default + PARAM_initMode);
		ClientInitMode defaultInitMode = ClientInitMode.fromString(defaultInitModeString, ClientInitMode.lazy);

		List<String> clientNamesRequiringEagerInitialization = new ArrayList<>();
		for(String name : DrCollectionTool.nullSafe(getClientNames())){
			String clientInitModeString = DrPropertiesTool.getFirstOccurrence(multiProperties, PREFIX_client + name
					+ PARAM_initMode);
			ClientInitMode mode = ClientInitMode.fromString(clientInitModeString, defaultInitMode);
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
		return lazyClientProviderByName.get(clientName).call();
	}

	public List<Client> getClients(Datarouter context, Collection<String> clientNames){
		List<Client> clients = DrListTool.createArrayListWithSize(clientNames);
		List<LazyClientProvider> providers = new ArrayList<>();//TODO don't create until needed
		for(String clientName : DrCollectionTool.nullSafe(clientNames)){
			LazyClientProvider provider = lazyClientProviderByName.get(clientName);
			if(provider.isInitialized()){
				clients.add(provider.call());//these can be added immediately (normal code path)
			}else{
				providers.add(provider);//these must be initialized first
			}
		}
		if(DrCollectionTool.notEmpty(providers)){
			clients.addAll(FutureTool.submitAndGetAll(providers, context.getExecutorService()));
		}
		return clients;
	}

	public List<Client> getAllClients(Datarouter context){
		return getClients(context, ClientId.getNames(clientIds));
	}


}

