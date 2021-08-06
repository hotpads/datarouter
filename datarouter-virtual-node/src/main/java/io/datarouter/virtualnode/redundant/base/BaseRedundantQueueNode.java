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
import io.datarouter.virtualnode.redundant.RedundantQueueNode;

public abstract class BaseRedundantQueueNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D,F>>
extends BaseNode<PK,D,F>
implements RedundantQueueNode<PK,D,F,N>{

	protected final N writeNode;
	protected final List<N> readNodes;

	public BaseRedundantQueueNode(N writeNode, List<N> readNodes){
		super(new NodeParamsBuilder<>(
				writeNode.getFieldInfo().getDatabeanSupplier(),
				writeNode.getFieldInfo().getFielderSupplier())
				.build());
		this.readNodes = Require.notEmpty(readNodes, "readNodes cannot be empty");
		this.writeNode = Objects.requireNonNull(writeNode);
		Require.contains(readNodes, writeNode, "writeNode must be in readNodes.");
	}

	@Override
	public String getName(){
		return Stream.concat(Stream.of(writeNode), readNodes.stream())
				.map(Node::getName)
				.collect(Collectors.joining(",", getClass().getSimpleName() + "[", "]"));
	}

	@Override
	public List<PhysicalNode<PK,D,F>> getPhysicalNodes(){
		return readNodes.stream()
				.map(N::getPhysicalNodes)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public List<PhysicalNode<PK,D,F>> getPhysicalNodesForClient(String clientName){
		return readNodes.stream()
				.map(backingNode -> backingNode.getPhysicalNodesForClient(clientName))
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public List<ClientId> getClientIds(){
		 return readNodes.stream()
				.map(N::getClientIds)
				.flatMap(Collection::stream)
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public boolean usesClient(String clientName){
		return readNodes.stream()
				.anyMatch(backingNode -> backingNode.usesClient(clientName));
	}

	@Override
	public List<N> getChildNodes(){
		return readNodes;
	}

	@Override
	public List<N> getReadNodes(){
		return readNodes;
	}

	@Override
	public N getWriteNode(){
		return writeNode;
	}

}
