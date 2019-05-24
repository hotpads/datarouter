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

import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterWriteBehindExecutor;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterWriteBehindScheduler;
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
	private final DatarouterWriteBehindScheduler writeBehindScheduler;
	private final DatarouterWriteBehindExecutor writeBehindExecutor;
	private final RouterClasses routerClasses;

	@Inject
	public Datarouter(DatarouterClients clients, DatarouterNodes nodes,
			DatarouterWriteBehindExecutor writeBehindExecutor, DatarouterWriteBehindScheduler writeBehindScheduler,
			RouterClasses routerClasses){
		this.clients = clients;
		this.nodes = nodes;
		this.writeBehindExecutor = writeBehindExecutor;
		this.writeBehindScheduler = writeBehindScheduler;
		this.routerClasses = routerClasses;
	}

	public synchronized void registerConfigFile(String configFilePath){
		clients.registerConfigFile(configFilePath);
	}

	public List<ClientId> registerClientIds(Collection<ClientId> clientIds){
		return clients.registerClientIds(clientIds);
	}

	public synchronized void register(Router router){
		if(!(router instanceof TestRouter) && !routerClasses.get().contains(router.getClass())){
			throw new IllegalArgumentException("Unknown router: " + router.getClass().getSimpleName()
					+ ". Please register it in RouterClasses or have it implement TestRouter if only used for tests");
		}
	}

	public void initializeEagerClients(){
		clients.initializeEagerClients();
	}

	public void shutdown(){
		clients.shutdown();
	}


	/*------------------------------- methods--------------------------------*/

	public NavigableSet<PhysicalNode<?,?,?>> getWritableNodes(){
		return clients.getClientIds().stream()
				.filter(ClientId::getWritable)
				.map(ClientId::getName)
				.map(nodes::getPhysicalNodesForClient)
				.flatMap(Collection::stream)
				.collect(Collectors.toCollection(TreeSet::new));
	}

	public DatarouterClients getClientPool(){
		return clients;
	}

	public DatarouterNodes getNodes(){
		return nodes;
	}

	public ExecutorService getWriteBehindExecutor(){
		return writeBehindExecutor;
	}

	public ScheduledExecutorService getWriteBehindScheduler(){
		return writeBehindScheduler;
	}

}
