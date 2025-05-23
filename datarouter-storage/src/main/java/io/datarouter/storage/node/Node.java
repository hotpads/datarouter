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
package io.datarouter.storage.node;

import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;

/**
 * A Node is the interface through which the application sends Databeans for serialization and storage. It ties together
 * a PrimaryKey type, a Databean type, a Fielder type. A Node can be a PhysicalNode or a virtual node, like
 * ReplicationNode, that forwards requests on to other nodes.
 */
public interface Node<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends Comparable<Node<PK,D,F>>{

	String getName();
	DatabeanFieldInfo<PK,D,F> getFieldInfo();
	List<ClientId> getClientIds();
	boolean usesClient(String clientName);
	List<? extends PhysicalNode<PK,D,F>> getPhysicalNodes();
	List<? extends PhysicalNode<PK,D,F>> getPhysicalNodesForClient(String clientName);
	List<? extends Node<PK,D,F>> getChildNodes();

}
