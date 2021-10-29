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
package io.datarouter.virtualnode.redundant.base;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.BaseNode;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.Require;
import io.datarouter.virtualnode.redundant.RedundantNode;

public abstract class BaseRedundantNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D,F>>
extends BaseNode<PK,D,F>
implements RedundantNode<PK,D,F,N>{

	protected final List<N> writeNodes;
	protected final N readNode;// needs to be one of the write nodes

	public BaseRedundantNode(List<N> writeNodes, N readNode){
		super(new NodeParamsBuilder<>(
				readNode.getFieldInfo().getDatabeanSupplier(),
				readNode.getFieldInfo().getFielderSupplier())
				.build());
		this.writeNodes = Require.notEmpty(writeNodes, "writeNodes cannot be empty");
		this.readNode = Objects.requireNonNull(readNode);
		Require.contains(writeNodes, readNode, "readNode must be in writeNodes.");
	}

	/*------------------------------ node methods ---------------------------*/

	@Override
	public String getName(){
		return Stream.concat(Stream.of(readNode), writeNodes.stream())
				.map(Node::getName)
				.collect(Collectors.joining(",", getClass().getSimpleName() + "[", "]"));
	}

	@Override
	public List<PhysicalNode<PK,D,F>> getPhysicalNodes(){
		return writeNodes.stream()
				.map(N::getPhysicalNodes)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public List<PhysicalNode<PK,D,F>> getPhysicalNodesForClient(String clientName){
		return writeNodes.stream()
				.map(backingNode -> backingNode.getPhysicalNodesForClient(clientName))
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public List<ClientId> getClientIds(){
		return writeNodes.stream()
				.map(N::getClientIds)
				.flatMap(Collection::stream)
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public boolean usesClient(String clientName){
		return writeNodes.stream()
				.anyMatch(backingNode -> backingNode.usesClient(clientName));
	}

	@Override
	public List<N> getChildNodes(){
		return writeNodes;
	}

	@Override
	public List<N> getWriteNodes(){
		return writeNodes;
	}

	@Override
	public N getReadNode(){
		return readNode;
	}

}
