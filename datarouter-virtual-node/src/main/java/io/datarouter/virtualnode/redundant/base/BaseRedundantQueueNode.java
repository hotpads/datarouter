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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
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
		Objects.requireNonNull(writeNode);
		Require.notEmpty(readNodes, "readNodes cannot be empty");
		this.readNodes = Scanner.of(readNodes)
				.reverse()//The writeNode is the new node; Read from the old node first.
				.list();
		this.writeNode = writeNode;
		Require.contains(readNodes, writeNode, "writeNode must be in readNodes.");
	}

	@Override
	public String getName(){
		return Scanner.of(writeNode)
				.append(readNodes)
				.map(Node::getName)
				.collect(Collectors.joining(",", getClass().getSimpleName() + "[", "]"));
	}

	@Override
	public List<PhysicalNode<PK,D,F>> getPhysicalNodes(){
		return Scanner.of(readNodes)
				.concatIter(N::getPhysicalNodes)
				.collect(Collectors.toList());
	}

	@Override
	public List<PhysicalNode<PK,D,F>> getPhysicalNodesForClient(String clientName){
		return Scanner.of(readNodes)
				.concatIter(backingNode -> backingNode.getPhysicalNodesForClient(clientName))
				.collect(Collectors.toList());
	}

	@Override
	public List<ClientId> getClientIds(){
		return Scanner.of(readNodes)
				.concatIter(N::getClientIds)
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public boolean usesClient(String clientName){
		return Scanner.of(readNodes)
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
