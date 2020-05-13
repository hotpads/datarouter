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
package io.datarouter.storage.node.type.index.base;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.BaseNode;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public abstract class BaseIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>,
		N extends Node<IK,IE,IF>>
extends BaseNode<IK,IE,IF>{

	/*----------------------------- node pass through -----------------------*/

	protected N indexNode;

	public BaseIndexNode(Supplier<IE> indexEntrySupplier, N backingNode){
		super(new NodeParamsBuilder<>(indexEntrySupplier, backingNode.getFieldInfo().getFielderSupplier()).build());
		this.indexNode = backingNode;
	}

	/*-------------------------------- methods ------------------------------*/

	public IE createIndexEntry(){
		return getFieldInfo().getDatabeanSupplier().get();
	}

	@Override
	public List<ClientId> getClientIds(){
		return indexNode.getClientIds();
	}

	@Override
	public List<? extends Node<IK,IE,IF>> getChildNodes(){
		return Scanner.ofNullable(indexNode).list();
	}

	@Override
	public String getName(){
		return indexNode == null ? null : indexNode.getName();
	}

	@Override
	public List<? extends PhysicalNode<IK,IE,IF>> getPhysicalNodes(){
		return indexNode.getPhysicalNodes();
	}

	@Override
	public List<? extends PhysicalNode<IK,IE,IF>> getPhysicalNodesForClient(String clientName){
		return indexNode.getPhysicalNodesForClient(clientName);
	}

	@Override
	public boolean usesClient(String clientName){
		return indexNode.usesClient(clientName);
	}

	public N getBackingNode(){
		return indexNode;
	}

}
