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

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientManager;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterWriteBehindExecutor;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterWriteBehindScheduler;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaoClasses;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.type.physical.PhysicalNode;

/**
 * Datarouter is the top-level scope through which various components can share things like clients,
 * configuration, and thread pools. Almost all applications will have a single Datarouter, though it is possible
 * to have multiple contexts for modularized applications or large test suites.
 */
@Singleton
public class Datarouter{

	private final DatarouterClients clients;
	private final DatarouterNodes nodes;
	private final DatarouterWriteBehindScheduler writeBehindScheduler;
	private final DatarouterWriteBehindExecutor writeBehindExecutor;
	private final DaoClasses daoClasses;

	@Inject
	public Datarouter(
			DatarouterClients clients,
			DatarouterNodes nodes,
			DatarouterWriteBehindExecutor writeBehindExecutor,
			DatarouterWriteBehindScheduler writeBehindScheduler,
			DaoClasses daoClasses){
		this.clients = clients;
		this.nodes = nodes;
		this.writeBehindExecutor = writeBehindExecutor;
		this.writeBehindScheduler = writeBehindScheduler;
		this.daoClasses = daoClasses;
	}

	public synchronized void registerConfigFile(String configFilePath){
		clients.registerConfigFile(configFilePath);
	}

	public List<ClientId> registerClientIds(Collection<ClientId> clientIds){
		return clients.registerClientIds(clientIds);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D,F>>
	N register(N node){
		nodes.register(node);
		for(ClientId clientId : registerClientIds(node.getClientIds())){
			ClientManager clientManager = clients.getClientManager(clientId);
			clientManager.doSchemaUpdate(node.getPhysicalNodesForClient(clientId.getName()));
		}
		return node;
	}

	public synchronized void assertRegistered(Dao router){
		if(!(router instanceof TestDao)
				&& !daoClasses.get().contains(router.getClass())){
			throw new IllegalArgumentException("Unknown router/dao: " + router.getClass().getSimpleName()
					+ ". Please register it in RouterClasses/DaoGroup, or have it implement TestRouter/TestDao "
					+ "if only used for tests");
		}
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
