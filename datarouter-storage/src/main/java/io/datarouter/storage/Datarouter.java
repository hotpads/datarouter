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
package io.datarouter.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.datarouter.storage.client.Client;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.client.LazyClientProvider;
import io.datarouter.storage.config.guice.DatarouterStorageExecutorGuiceModule;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.router.Router;
import io.datarouter.storage.router.RouterClasses;
import io.datarouter.storage.router.TestRouter;

/**
 * Datarouter is the top-level scope through which various components can share things like clients,
 * configuration, and thread pools. Almost all applications will have a single Datarouter, though it is possible
 * to have multiple contexts for modularized applications or large test suites.
 */
@Singleton
public class Datarouter{

	//injected
	private final DatarouterClients clients;
	private final DatarouterNodes nodes;
	private final ExecutorService executorService;//for async client init and monitoring
	private final ScheduledExecutorService writeBehindScheduler;
	private final ExecutorService writeBehindExecutor;
	private final RouterClasses routerClasses;

	private SortedSet<Router> routers;

	@Inject
	public Datarouter(
			DatarouterClients clients,
			DatarouterNodes nodes,
			@Named(DatarouterStorageExecutorGuiceModule.POOL_datarouterExecutor) ExecutorService executorService,
			@Named(DatarouterStorageExecutorGuiceModule.POOL_writeBehindExecutor) ExecutorService writeBehindExecutor,
			@Named(DatarouterStorageExecutorGuiceModule.POOL_writeBehindScheduler) ScheduledExecutorService
				writeBehindScheduler, RouterClasses routerClasses){
		this.executorService = executorService;
		this.clients = clients;
		this.nodes = nodes;
		this.writeBehindExecutor = writeBehindExecutor;
		this.writeBehindScheduler = writeBehindScheduler;
		this.routerClasses = routerClasses;
		this.routers = new TreeSet<>();

		this.nodes.registerDatarouter(this);
	}

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
		if(routers.contains(router)){
			throw new RuntimeException(router.getName() + " router has already been registered");
		}
		routers.add(router);
	}

	public void initializeEagerClients(){
		clients.initializeEagerClients();
	}

	public void shutdown(){
		clients.shutdown();
		executorService.shutdown();
	}


	/*------------------------------- methods--------------------------------*/

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

}
