/*
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
package io.datarouter.virtualnode.replication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.BaseNode;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public abstract class BaseReplicationNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D,F>>
extends BaseNode<PK,D,F>
implements ReplicationNode<PK,D,F,N>{

	protected final N primary;
	protected final List<N> replicas;
	protected final List<N> primaryAndReplicas;
	protected final AtomicInteger replicaRequestCounter;

	public BaseReplicationNode(N primary, Collection<N> replicas){
		super(new NodeParamsBuilder<>(
				primary.getFieldInfo().getDatabeanSupplier(),
				primary.getFieldInfo().getFielderSupplier())
				.build());
		this.replicas = new ArrayList<>();
		this.primaryAndReplicas = new ArrayList<>();
		this.replicaRequestCounter = new AtomicInteger(0);
		this.primary = primary;
		this.primaryAndReplicas.add(primary);
		this.replicas.addAll(replicas);
		this.primaryAndReplicas.addAll(replicas);
	}

	/*--------------- node methods -------------------*/

	@Override
	public String getName(){
		return primaryAndReplicas.stream()
				.map(Node::getName)
				.collect(Collectors.joining(",", getClass().getSimpleName() + "[", "]"));
	}

	@Override
	public List<PhysicalNode<PK,D,F>> getPhysicalNodes(){
		List<PhysicalNode<PK,D,F>> all = new ArrayList<>();
		all.addAll(primary.getPhysicalNodes());
		replicas.stream()
				.map(N::getPhysicalNodes)
				.forEach(all::addAll);
		return all;
	}

	@Override
	public List<PhysicalNode<PK,D,F>> getPhysicalNodesForClient(String clientName){
		List<PhysicalNode<PK,D,F>> all = new ArrayList<>();
		all.addAll(primary.getPhysicalNodesForClient(clientName));
		replicas.stream()
				.map(replica -> replica.getPhysicalNodesForClient(clientName))
				.forEach(all::addAll);
		return all;
	}

	@Override
	public List<ClientId> getClientIds(){
		Set<ClientId> clientIds = new HashSet<>(primary.getClientIds());
		replicas.stream()
				.map(N::getClientIds)
				.forEach(clientIds::addAll);
		return new ArrayList<>(clientIds);
	}

	@Override
	public boolean usesClient(String clientName){
		if(primary.usesClient(clientName)){
			return true;
		}
		return replicas.stream()
				.filter(replica -> replica.usesClient(clientName))
				.findAny()
				.isPresent();
	}

	/*---------------- Replication methods ---------------------*/

	@Override
	public N getPrimary(){
		return primary;
	}

	@Override
	public List<N> getChildNodes(){
		return primaryAndReplicas;
	}

	@Override
	public N chooseReplica(Config config){
		if(replicas.isEmpty()){
			return primary;
		}
		int replicaIndex = replicaRequestCounter.incrementAndGet() % replicas.size();
		return replicas.get(replicaIndex);
	}

}
