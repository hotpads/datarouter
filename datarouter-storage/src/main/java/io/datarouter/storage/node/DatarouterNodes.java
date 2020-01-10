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

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

/**
 * Nodes is a registry of all Nodes in a Datarouter. It ensures that no two nodes try to share the same name. It can be
 * used by Datarouter management features like a web page to browse all nodes in the system.
 */
@Singleton
public class DatarouterNodes{

	/*------------------------------ fields ---------------------------------*/

	private final SortedSet<Node<?,?,?>> topLevelNodes;
	private final Map<String,Node<?,?,?>> nodeByName;
	private final Map<String,Map<String,PhysicalNode<?,?,?>>> physicalNodeByTableNameByClientName;

	/*---------------------------- constructor ------------------------------*/

	DatarouterNodes(){
		this.topLevelNodes = new ConcurrentSkipListSet<>();
		this.nodeByName = new ConcurrentSkipListMap<>();
		this.physicalNodeByTableNameByClientName = new ConcurrentSkipListMap<>();
	}

	/*------------------------------ methods --------------------------------*/

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D,F>>
	N register(N node){
		ensureDuplicateNamesReferToSameNode(node);
		List<Node<?,?,?>> nodeWithDescendants = NodeTool.getNodeAndDescendants(node);
		topLevelNodes.add(node);
		for(Node<?,?,?> nodeOrDescendant : nodeWithDescendants){
			nodeByName.put(nodeOrDescendant.getName(), nodeOrDescendant);
			if(nodeOrDescendant instanceof PhysicalNode<?,?,?>){
				PhysicalNode<?,?,?> physicalNode = (PhysicalNode<?,?,?>)nodeOrDescendant;
				String clientName = physicalNode.getFieldInfo().getClientId().getName();
				String tableName = physicalNode.getFieldInfo().getTableName();
				physicalNodeByTableNameByClientName.computeIfAbsent(clientName, k -> new TreeMap<>()).put(tableName,
						physicalNode);
			}
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

	public Set<Class<?>> getTypesForClient(String clientName){
		return nodeByName.values().stream()
				.filter(node -> node.usesClient(clientName))
				.map(Node::getFieldInfo)
				.map(DatabeanFieldInfo::getSampleDatabean)
				.map(Databean::getClass)
				.collect(Collectors.toSet());
	}

	public Collection<PhysicalNode<?,?,?>> getPhysicalNodesForClient(String clientName){
		return topLevelNodes.stream()
				.map(node -> node.getPhysicalNodesForClient(clientName))
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	public List<String> getTableNamesForClient(String clientName){
		return getPhysicalNodesForClient(clientName).stream()
				.map(PhysicalNode::getFieldInfo)
				.map(PhysicalDatabeanFieldInfo::getTableName)
				.distinct()
				.collect(Collectors.toList());
	}

	private void ensureDuplicateNamesReferToSameNode(Node<?,?,?> node){
		String thisName = node.getName();
		Node<?,?,?> existingNode = nodeByName.get(thisName);
		if(existingNode == null || existingNode == node){
			return;
		}
		String existingNodeSimpleName = existingNode.getClass().getSimpleName();
		throw new IllegalArgumentException("different node with this name already exists:" + thisName + "["
				+ existingNodeSimpleName + "].");
	}

	public PhysicalNode<?,?,?> getPhysicalNodeForClientAndTable(String clientName, String tableName){
		return physicalNodeByTableNameByClientName.getOrDefault(clientName, Collections.emptyMap()).get(tableName);
	}

	public Map<String,Map<String,PhysicalNode<?,?,?>>> getPhysicalNodeByTableNameByClientName(){
		return physicalNodeByTableNameByClientName;
	}

	public Node<?,?,?> findParent(Node<?,?,?> node, Class<?> requiredInterface){
		for(Node<?,?,?> topLevelNode : topLevelNodes){
			if(topLevelNode == node){
				return node;
			}
			Node<?,?,?> foundParent = findParent(node, new LinkedList<>(List.of(topLevelNode)), requiredInterface);
			if(foundParent != null){
				return foundParent;
			}
		}
		throw new RuntimeException(node + " assignable to " + requiredInterface + " not found");
	}

	private static Node<?,?,?> findParent(Node<?,?,?> node, Deque<Node<?,?,?>> parents, Class<?> requiredInterface){
		for(Node<?,?,?> childNode : parents.peekLast().getChildNodes()){
			if(childNode == node){
				Node<?,?,?> parent;
				while((parent = parents.pollFirst()) != null){
					if(requiredInterface.isAssignableFrom(parent.getClass())){
						return parent;
					}
				}
				return childNode;
			}
			Deque<Node<?,?,?>> parentsCopy = new LinkedList<>(parents);
			parentsCopy.addLast(childNode);
			Node<?,?,?> foundParent = findParent(node, parentsCopy, requiredInterface);
			if(foundParent != null){
				return foundParent;
			}
		}
		return null;
	}

	/*------------------------------ get/set --------------------------------*/

	public SortedSet<Node<?,?,?>> getTopLevelNodes(){
		return topLevelNodes;
	}

}
