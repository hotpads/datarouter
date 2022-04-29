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
package io.datarouter.storage.node.type.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.client.ClientInitializationTracker;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

@Singleton
public class ManagedNodesHolder{

	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private ClientInitializationTracker clientInitializationTracker;

	private final Map<PhysicalDatabeanFieldInfo<?,?,?>,List<ManagedNode<?,?,?,?,?>>> managedNodes
			= new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	List<ManagedNode<PK,D,?,?,?>> getManagedNodes(PhysicalDatabeanFieldInfo<PK,D,?> fieldInfo){
		return managedNodes.computeIfAbsent(
						fieldInfo,
						$ -> new ArrayList<>()).stream()
				.map(node -> (ManagedNode<PK,D,?,?,?>)node)
				.collect(Collectors.toList());
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends ManagedNode<PK,D,?,?,?>>
	N registerManagedNode(PhysicalDatabeanFieldInfo<PK,D,?> fieldInfo, N managedNode){
		boolean isRegistered = datarouterNodes.getAllNodes().stream()
				.map(Node::getName)
				.anyMatch(fieldInfo.getNodeName()::equals);
		boolean isClientInitialized = clientInitializationTracker.isInitialized(fieldInfo.getClientId());
		if(isRegistered && isClientInitialized){
			throw new RuntimeException(this + " is already registered and initialized, can't register index "
					+ managedNode);
		}
		managedNodes.computeIfAbsent(fieldInfo, $ -> new ArrayList<>()).add(managedNode);
		return managedNode;
	}

}
