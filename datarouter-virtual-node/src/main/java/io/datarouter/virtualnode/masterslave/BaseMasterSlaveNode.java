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
package io.datarouter.virtualnode.masterslave;

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

public abstract class BaseMasterSlaveNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D,F>>
extends BaseNode<PK,D,F>
implements MasterSlaveNode<PK,D,F,N>{

	protected final N master;
	protected final List<N> slaves;
	protected final List<N> masterAndSlaves;
	protected final AtomicInteger slaveRequestCounter;

	public BaseMasterSlaveNode(N master, Collection<N> slaves){
		super(new NodeParamsBuilder<>(
				master.getFieldInfo().getDatabeanSupplier(),
				master.getFieldInfo().getFielderSupplier())
				.build());
		this.slaves = new ArrayList<>();
		this.masterAndSlaves = new ArrayList<>();
		this.slaveRequestCounter = new AtomicInteger(0);
		this.master = master;
		this.masterAndSlaves.add(master);
		this.slaves.addAll(slaves);
		this.masterAndSlaves.addAll(slaves);
	}

	/*--------------- node methods -------------------*/

	@Override
	public String getName(){
		return masterAndSlaves.stream()
				.map(Node::getName)
				.collect(Collectors.joining(",", getClass().getSimpleName() + "[", "]"));
	}

	@Override
	public List<PhysicalNode<PK,D,F>> getPhysicalNodes(){
		List<PhysicalNode<PK,D,F>> all = new ArrayList<>();
		all.addAll(master.getPhysicalNodes());
		slaves.stream()
				.map(N::getPhysicalNodes)
				.forEach(all::addAll);
		return all;
	}

	@Override
	public List<PhysicalNode<PK,D,F>> getPhysicalNodesForClient(String clientName){
		List<PhysicalNode<PK,D,F>> all = new ArrayList<>();
		all.addAll(master.getPhysicalNodesForClient(clientName));
		slaves.stream()
				.map(slave -> slave.getPhysicalNodesForClient(clientName))
				.forEach(all::addAll);
		return all;
	}

	@Override
	public List<ClientId> getClientIds(){
		Set<ClientId> clientIds = new HashSet<>(master.getClientIds());
		slaves.stream()
				.map(N::getClientIds)
				.forEach(clientIds::addAll);
		return new ArrayList<>(clientIds);
	}

	@Override
	public boolean usesClient(String clientName){
		if(master.usesClient(clientName)){
			return true;
		}
		return slaves.stream()
				.filter(slave -> slave.usesClient(clientName))
				.findAny()
				.isPresent();
	}

	/*---------------- MasterSlaveNode methods ---------------------*/

	@Override
	public N getMaster(){
		return master;
	}

	@Override
	public List<N> getChildNodes(){
		return masterAndSlaves;
	}

	@Override
	public N chooseSlave(Config config){
		if(slaves.isEmpty()){
			return master;
		}
		int slaveIndex = slaveRequestCounter.incrementAndGet() % slaves.size();
		return slaves.get(slaveIndex);
	}

}
