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
package io.datarouter.storage.node.type.indexing.base;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.BaseNode;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.op.raw.index.IndexListener;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public abstract class BaseIndexingNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D,F>>
extends BaseNode<PK,D,F>{

	protected final N mainNode;
	protected final List<IndexListener<PK,D>> indexListeners;

	public BaseIndexingNode(N mainNode){
		super(new NodeParamsBuilder<>(
				mainNode.getFieldInfo().getDatabeanSupplier(),
				mainNode.getFieldInfo().getFielderSupplier()).build());
		this.mainNode = mainNode;
		this.indexListeners = new ArrayList<>();
	}

	public void registerIndexListener(IndexListener<PK,D> indexListener){
		this.indexListeners.add(indexListener);
	}

	/*--------------------------- node methods ------------------------------*/

	// TODO allow indexes to be on different clients than the primary node

	@Override
	public final String getName(){
		return getClass().getSimpleName() + "[" + mainNode.getName() + "]";
	}

	@Override
	public List<PhysicalNode<PK,D,F>> getPhysicalNodes(){
		return new ArrayList<>(mainNode.getPhysicalNodes());
	}

	@Override
	public List<PhysicalNode<PK,D,F>> getPhysicalNodesForClient(String clientName){
		return new ArrayList<>(mainNode.getPhysicalNodesForClient(clientName));
	}

	@Override
	public List<ClientId> getClientIds(){
		return mainNode.getClientIds();
	}

	@Override
	public boolean usesClient(String clientName){
		return mainNode.usesClient(clientName);
	}

	@Override
	public List<N> getChildNodes(){
		return Scanner.ofNullable(mainNode).list();
	}

	public N getBackingNode(){
		return mainNode;
	}

	public List<IndexListener<PK,D>> getIndexNodes(){
		return indexListeners;
	}

}
