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
package io.datarouter.storage.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.inject.Singleton;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.collection.CollectionTool;

/**
 * Nodes is a registry of all Nodes in a Datarouter. It ensures that no two nodes try to share the same name. It can be
 * used by Datarouter management features like a web page to browse all nodes in the system.
 */
@Singleton
public class DatarouterNodes{

	/*------------------------------ fields ---------------------------------*/

	private final SortedSet<Node<?,?,?>> topLevelNodes;
	private final Map<String,Node<?,?,?>> nodeByName;
	private final Multimap<String,Node<?,?,?>> topLevelNodesByRouterName;
	private final Map<Node<?,?,?>,String> routerNameByNode;
	private final Map<String,Set<ClientId>> clientIdsByRouterName;
	private final Map<String,Map<String,PhysicalNode<?,?,?>>> physicalNodeByTableNameByClientName;
	private Datarouter datarouter;

	/*---------------------------- constructor ------------------------------*/

	DatarouterNodes(){
		this.topLevelNodes = new ConcurrentSkipListSet<>();
		this.nodeByName = new ConcurrentSkipListMap<>();
		this.topLevelNodesByRouterName = Multimaps.synchronizedMultimap(TreeMultimap.create());
		this.routerNameByNode = new ConcurrentSkipListMap<>();
		this.clientIdsByRouterName = new ConcurrentSkipListMap<>();
		this.physicalNodeByTableNameByClientName = new ConcurrentSkipListMap<>();
	}

	public void registerDatarouter(Datarouter datarouter){
		this.datarouter = datarouter;
	}

	/*------------------------------ methods --------------------------------*/

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D,F>>
	N register(String routerName, N node){
		ensureDuplicateNamesReferToSameNode(routerName, node);
		List<Node<?,?,?>> nodeWithDescendants = NodeTool.getNodeAndDescendants(node);
		this.topLevelNodes.add(node);
		this.topLevelNodesByRouterName.put(routerName, node);
		for(Node<?,?,?> nodeOrDescendant : nodeWithDescendants){
			nodeByName.put(nodeOrDescendant.getName(), nodeOrDescendant);
			if(nodeOrDescendant instanceof PhysicalNode<?,?,?>){
				PhysicalNode<?,?,?> physicalNode = (PhysicalNode<?,?,?>)nodeOrDescendant;
				String clientName = physicalNode.getFieldInfo().getClientId().getName();
				String tableName = physicalNode.getFieldInfo().getTableName();
				physicalNodeByTableNameByClientName.computeIfAbsent(clientName, k -> new TreeMap<>()).put(tableName,
						physicalNode);
			}
			routerNameByNode.put(nodeOrDescendant, routerName);
			clientIdsByRouterName.computeIfAbsent(routerName, k -> new TreeSet<>()).addAll(node.getClientIds());
		}

		return node;
	}

	public Collection<Node<?,?,?>> getAllNodes(){
		return nodeByName.values();
	}

	public Node<?,?,?> getNode(String nodeName){
		return nodeByName.get(nodeName);
	}

	@SuppressWarnings("unchecked")
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			N extends Node<PK,D,?>>
	N getNodeAndCast(String nodeName){
		return (N)getNode(nodeName);
	}

	public List<ClientId> getClientIdsForRouter(String routerName){
		return new ArrayList<>(CollectionTool.nullSafe(clientIdsByRouterName.get(routerName)));
	}

	public Set<Class<?>> getTypesForClient(String clientName){
		Set<Class<?>> types = new HashSet<>();
		for(Node<?,?,?> node : nodeByName.values()){
			if(node.usesClient(clientName)){
				types.add(node.getFieldInfo().getSampleDatabean().getClass());
			}
		}
		return types;
	}

	public Collection<PhysicalNode<?,?,?>> getPhysicalNodesForClient(String clientName){
		SortedSet<PhysicalNode<?,?,?>> physicalNodesForClient = new TreeSet<>();
		for(Node<?,?,?> node : topLevelNodes){
			List<? extends PhysicalNode<?,?,?>> physicalNodesForNode = node.getPhysicalNodesForClient(clientName);
			for(PhysicalNode<?,?,?> physicalNode : physicalNodesForNode){
				physicalNodesForClient.add(physicalNode);
			}
		}
		return physicalNodesForClient;
	}

	public List<String> getTableNamesForClient(String clientName){
		Set<String> tableNames = new TreeSet<>();
		for(PhysicalNode<?,?,?> physicalNode : getPhysicalNodesForClient(clientName)){
			tableNames.add(physicalNode.getFieldInfo().getTableName());
		}
		return new ArrayList<>(tableNames);
	}

	public List<String> getTableNamesForRouterAndClient(String routerName, String clientName){
		List<String> tableNames = new ArrayList<>();
		for(PhysicalNode<?,?,?> physicalNode : getPhysicalNodesForClient(clientName)){
			if(Objects.equals(routerNameByNode.get(physicalNode), routerName)){
				tableNames.add(physicalNode.getFieldInfo().getTableName());
			}
		}
		return tableNames;
	}

	private void ensureDuplicateNamesReferToSameNode(String routerName, Node<?,?,?> node){
		String thisName = node.getName();
		Node<?,?,?> existingNode = nodeByName.get(thisName);
		if(existingNode == null || existingNode == node){
			return;
		}
		String existingRouter = findFqRouterClassName(routerNameByNode.get(node));
		String newRouter = findFqRouterClassName(routerName);
		String existingNodeSimpleName = existingNode.getClass().getSimpleName();
		throw new IllegalArgumentException("different node with this name already exists:" + thisName + "["
				+ existingNodeSimpleName + "] in " + existingRouter + ". Add attempted by " + newRouter);
	}

	private String findFqRouterClassName(String routerName){
		return datarouter.getRouter(routerName).getClass().getName();
	}

	public PhysicalNode<?,?,?> getPhyiscalNodeForClientAndTable(String clientName, String tableName){
		return physicalNodeByTableNameByClientName.getOrDefault(clientName, Collections.emptyMap()).get(tableName);
	}

	/*------------------------------ get/set --------------------------------*/

	public Multimap<String,Node<?,?,?>> getTopLevelNodesByRouterName(){
		return topLevelNodesByRouterName;
	}

}
