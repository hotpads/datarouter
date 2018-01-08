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
package io.datarouter.storage.routing;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.client.Client;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.client.LazyClientProvider;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.guice.DatarouterExecutorGuiceModule;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.properties.PropertiesTool;
import io.datarouter.util.tuple.Pair;

/**
 * Datarouter is the top-level scope through which various components can share things like clients,
 * configuration, and thread pools. Almost all applications will have a single Datarouter, though it is possible
 * to have multiple contexts for modularized applications or large test suites.
 */
@Singleton
public class Datarouter{
	private static final Logger logger = LoggerFactory.getLogger(Datarouter.class);

	/*************************** fields *****************************/

	//injected
	private final DatarouterProperties datarouterProperties;
	private final DatarouterClients clients;
	private final DatarouterNodes nodes;
	private final ExecutorService executorService;//for async client init and monitoring
	private final ScheduledExecutorService writeBehindScheduler;
	private final ExecutorService writeBehindExecutor;
	private final RouterClasses routerClasses;

	private SortedSet<Router> routers;
	private Set<String> configFilePaths;
	private List<Properties> multiProperties;


	/************************** constructors ***************************/

	@Inject
	public Datarouter(
			DatarouterProperties datarouterProperties,
			DatarouterClients clients,
			DatarouterNodes nodes,
			@Named(DatarouterExecutorGuiceModule.POOL_datarouterExecutor) ExecutorService executorService,
			@Named(DatarouterExecutorGuiceModule.POOL_writeBehindExecutor) ExecutorService writeBehindExecutor,
			@Named(DatarouterExecutorGuiceModule.POOL_writeBehindScheduler) ScheduledExecutorService
				writeBehindScheduler, RouterClasses routerClasses){
		this.datarouterProperties = datarouterProperties;
		this.executorService = executorService;
		this.clients = clients;
		this.nodes = nodes;
		this.writeBehindExecutor = writeBehindExecutor;
		this.writeBehindScheduler = writeBehindScheduler;
		this.routerClasses = routerClasses;
		this.configFilePaths = new TreeSet<>();
		this.multiProperties = new ArrayList<>();
		this.routers = new TreeSet<>();

		this.nodes.registerDatarouter(this);
	}


	/********************** builder methods ****************************/

	public synchronized void registerConfigFile(String configFilePath){
		clients.registerConfigFile(configFilePath);
	}

	public Stream<LazyClientProvider> registerClientIds(Collection<ClientId> clientIds){
		return clients.registerClientIds(clientIds);
	}

	public synchronized void register(Router router){
		if(!(router instanceof TestRouter) && !routerClasses.get().contains(router.getClass())){
			throw new IllegalArgumentException("Unknown router: " + router.getClass().getSimpleName()
					+ ". Please register it in RouterClasses or have it implement TestRouter if only used for tests");
		}
		routers.add(router);
		addConfigIfNew(router);
	}

	private void addConfigIfNew(Router router){
		String configPath = router.getConfigLocation();
		if(configFilePaths.contains(configPath)){
			return;
		}
		Pair<Properties,URL> propertiesAndLocation = PropertiesTool.parseAndGetLocation(configPath);
		logger.warn("adding router config from " + propertiesAndLocation.getRight() + ", currentRouters:" + routers);
		configFilePaths.add(configPath);
		multiProperties.add(propertiesAndLocation.getLeft());
	}

	public void initializeEagerClients(){
		clients.initializeEagerClients();
	}

	public void shutdown(){
		clients.shutdown();
		executorService.shutdown();
	}


	/********************* methods **********************************/

	public Router getRouter(String name){
		for(Router router : routers){
			if(name.equals(router.getName())){
				return router;
			}
		}
		return null;
	}

	public List<Client> getClients(){
		SortedSet<Client> clients = new TreeSet<>();
		for(Router router : routers){
			for(Client client : router.getAllClients()){
				clients.add(client);
			}
		}
		return new ArrayList<>(clients);
	}

	public Router getRouterForClient(Client client){
		for(Router router : routers){
			for(Client c : router.getAllClients()){
				if(c == client){
					return router;
				}
			}
		}
		return null;
	}

	public NavigableSet<PhysicalNode<?,?,?>> getWritableNodes(){
		NavigableSet<PhysicalNode<?,?,?>> writableNodes = new TreeSet<>();
		for(Router router : routers){
			for(ClientId clientId : router.getClientIds()){
				if(!clientId.getWritable()){
					continue;
				}
				writableNodes.addAll(getNodes().getPhysicalNodesForClient(clientId.getName()));
			}
		}
		return writableNodes;
	}

	public DatarouterClients getClientPool(){
		return clients;
	}

	public DatarouterNodes getNodes(){
		return nodes;
	}

	public SortedSet<Router> getRouters(){
		return routers;
	}

	public ExecutorService getExecutorService(){
		return executorService;
	}

	public ExecutorService getWriteBehindExecutor(){
		return writeBehindExecutor;
	}

	public ScheduledExecutorService getWriteBehindScheduler(){
		return writeBehindScheduler;
	}

	public Set<String> getConfigFilePaths(){
		return configFilePaths;
	}

	public DatarouterProperties getDatarouterProperties(){
		return datarouterProperties;
	}

}
