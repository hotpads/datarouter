/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage.client;

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

import io.datarouter.storage.config.guice.DatarouterExecutorGuiceModule;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.util.concurrent.FutureTool;
import io.datarouter.util.properties.PropertiesTool;
import io.datarouter.util.string.StringTool;

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
	private final DatarouterNodes datarouterNodes;
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
	public DatarouterClients(DatarouterNodes datarouterNodes, ClientTypeRegistry clientTypeRegistry,
			@Named(DatarouterExecutorGuiceModule.POOL_datarouterExecutor) ExecutorService executorService){
		this.datarouterNodes = datarouterNodes;
		this.clientTypeRegistry = clientTypeRegistry;
		this.executorService = executorService;
		this.configFilePaths = new TreeSet<>();
		this.multiProperties = new ArrayList<>();
		this.clientIdByClientName = new TreeMap<>();
		this.lazyClientProviderByName = new ConcurrentHashMap<>();
		this.routerOptions = new RouterOptions(multiProperties);
	}

	public void registerConfigFile(String configFilePath){
		if(StringTool.notEmpty(configFilePath) && !configFilePaths.contains(configFilePath)){
			configFilePaths.add(configFilePath);
			multiProperties.add(PropertiesTool.parse(configFilePath));
			routerOptions = new RouterOptions(multiProperties);
		}
	}

	public Stream<LazyClientProvider> registerClientIds(Collection<ClientId> clientIdsToAdd){
		clientIdsToAdd.forEach(clientId -> clientIdByClientName.put(clientId.getName(), clientId));
		return clientIdsToAdd.stream()
				.map(ClientId::getName)
				.map(name -> initClientFactoryIfNull(name));
	}

	/********************************** initialize ******************************/

	public void initializeEagerClients(){
		getClients(getClientNamesRequiringEagerInitialization());
	}

	public ClientType getClientTypeInstance(String clientName){
		return clientTypeRegistry.create(routerOptions.getClientType(clientName));
	}

	private synchronized LazyClientProvider initClientFactoryIfNull(String clientName){
		return lazyClientProviderByName.computeIfAbsent(clientName, client -> {
			ClientType clientTypeInstance = getClientTypeInstance(client);
			ClientFactory clientFactory = clientTypeInstance.createClientFactory(client);
			return new LazyClientProvider(clientFactory, datarouterNodes);
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
		String forceInitModeString = PropertiesTool.getFirstOccurrence(multiProperties, PREFIX_clients
				+ PARAM_forceInitMode);
		ClientInitMode forceInitMode = ClientInitMode.fromString(forceInitModeString, null);
		if(forceInitMode != null){
			if(ClientInitMode.eager == forceInitMode){
				return getClientNames();
			}
			return Collections.emptyList();
		}

		String defaultInitModeString = PropertiesTool.getFirstOccurrence(multiProperties, PREFIX_client
				+ CLIENT_default + PARAM_initMode);
		ClientInitMode defaultInitMode = ClientInitMode.fromString(defaultInitModeString, ClientInitMode.lazy);

		List<String> clientNamesRequiringEagerInitialization = new ArrayList<>();
		for(String name : getClientNames()){
			String clientInitModeString = PropertiesTool.getFirstOccurrence(multiProperties, PREFIX_client + name
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

	public Collection<Properties> getMultiProperties(){
		return multiProperties;
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
