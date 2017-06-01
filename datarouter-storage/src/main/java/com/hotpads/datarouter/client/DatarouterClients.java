package com.hotpads.datarouter.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.inject.guice.executor.DatarouterExecutorGuiceModule;
import com.hotpads.datarouter.routing.Datarouter;
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
		PARAM_initMode = ".initMode";

	//injected
	private final ClientTypeRegistry clientTypeRegistry;
	private final ExecutorService executorService;

	//not injected
	private final Set<String> configFilePaths;
	private final Collection<Properties> multiProperties;
	private final Map<String,ClientId> clientIdByClientName;
	private final Map<String,LazyClientProvider> lazyClientProviderByName;

	private RouterOptions routerOptions;

	/******************************* constructors **********************************/

	@Inject
	public DatarouterClients(ClientTypeRegistry clientTypeRegistry,
			@Named(DatarouterExecutorGuiceModule.POOL_datarouterExecutor) ExecutorService executorService){
		this.clientTypeRegistry = clientTypeRegistry;
		this.executorService = executorService;
		this.configFilePaths = new TreeSet<>();
		this.multiProperties = new ArrayList<>();
		this.clientIdByClientName = new TreeMap<>();
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

	public Stream<LazyClientProvider> registerClientIds(DatarouterProperties datarouterProperties,
			Datarouter datarouter, Collection<ClientId> clientIdsToAdd){
		clientIdsToAdd.forEach(clientId -> clientIdByClientName.put(clientId.getName(), clientId));
		return clientIdsToAdd.stream()
				.map(ClientId::getName)
				.map(name -> initClientFactoryIfNull(datarouterProperties, datarouter, name));
	}

	/********************************** initialize ******************************/

	public void initializeEagerClients(){
		getClients(getClientNamesRequiringEagerInitialization());
	}

	public ClientType getClientTypeInstance(String clientName){
		return clientTypeRegistry.create(routerOptions.getClientType(clientName));
	}

	private synchronized LazyClientProvider initClientFactoryIfNull(DatarouterProperties datarouterProperties,
			Datarouter datarouter, String clientName){
		return lazyClientProviderByName.computeIfAbsent(clientName, client -> {
			ClientType clientTypeInstance = getClientTypeInstance(client);
			ClientFactory clientFactory = clientTypeInstance.createClientFactory(datarouterProperties, datarouter,
					client);
			return new LazyClientProvider(clientFactory, datarouter.getNodes());
		});
	}


	/******************** shutdown ********************************************/

	//TODO shutdown clients in parallel
	public void shutdown(){
		for(LazyClientProvider lazyClientProvider : lazyClientProviderByName.values()){
			if(!lazyClientProvider.isInitialized()){
				continue;
			}
			Client client = lazyClientProvider.call();
			try{
				client.shutdown();
			}catch(Exception e){
				logger.warn("swallowing exception while shutting down client " + client.getName(), e);
			}
		}
	}


	/******************** getNames **********************************************/

	private Collection<String> getClientNamesRequiringEagerInitialization(){
		String forceInitModeString = DrPropertiesTool.getFirstOccurrence(multiProperties, PREFIX_clients
				+ PARAM_forceInitMode);
		ClientInitMode forceInitMode = ClientInitMode.fromString(forceInitModeString, null);
		if(forceInitMode != null){
			if(ClientInitMode.eager == forceInitMode){
				return getClientNames();
			}
			return Collections.emptyList();
		}

		String defaultInitModeString = DrPropertiesTool.getFirstOccurrence(multiProperties, PREFIX_client
				+ CLIENT_default + PARAM_initMode);
		ClientInitMode defaultInitMode = ClientInitMode.fromString(defaultInitModeString, ClientInitMode.lazy);

		List<String> clientNamesRequiringEagerInitialization = new ArrayList<>();
		for(String name : getClientNames()){
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

	public ClientId getClientId(String clientName){
		return clientIdByClientName.get(clientName);
	}

	public Set<String> getClientNames(){
		return clientIdByClientName.keySet();
	}

	public Map<Boolean,List<String>> getClientNamesByInitialized(){
		Function<Entry<String,LazyClientProvider>,Boolean> isInitialized = entry -> entry.getValue().isInitialized();
		return lazyClientProviderByName.entrySet().stream()
				.collect(Collectors.groupingBy(isInitialized, Collectors.mapping(Entry::getKey, Collectors.toList())));
	}

	public Client getClient(String clientName){
		return lazyClientProviderByName.get(clientName).call();
	}

	public List<Client> getClients(Collection<String> clientNames){
		List<Client> clients = new ArrayList<>();
		List<LazyClientProvider> providers = new ArrayList<>();//TODO don't create until needed
		for(String clientName : clientNames){
			LazyClientProvider provider = lazyClientProviderByName.get(clientName);
			Objects.requireNonNull(provider, "LazyClientProvider cannot be null for clientName=" + clientName);
			if(provider.isInitialized()){
				clients.add(provider.call());//these can be added immediately (normal code path)
			}else{
				providers.add(provider);//these must be initialized first
			}
		}
		clients.addAll(FutureTool.submitAndGetAll(providers, executorService));
		return clients;
	}

	public List<Client> getAllClients(){
		return getClients(getClientNames());
	}

	public Map<String,LazyClientProvider> getLazyClientProviderByName(){
		return lazyClientProviderByName;
	}

}
