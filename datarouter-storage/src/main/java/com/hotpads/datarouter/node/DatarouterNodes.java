package com.hotpads.datarouter.node;

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

import javax.inject.Singleton;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

/**
 * Nodes is a registry of all Nodes in a Datarouter. It ensures that no two nodes try to share the same name. It can be
 * used by Datarouter management features like a web page to browse all nodes in the system.
 */
@Singleton
public class DatarouterNodes{

	/************************ fields *******************************/

	private final SortedSet<Node<?,?>> topLevelNodes;
	private final Map<String,Node<?,?>> nodeByName;
	private final Multimap<String,Node<?,?>> topLevelNodesByRouterName;
	private final Map<Node<?,?>,String> routerNameByNode;
	private final Map<String,Set<ClientId>> clientIdsByRouterName;
	private final Map<String,Map<String,PhysicalNode<?,?>>> physicalNodeByTableNameByClientName;

	/********************** constructors **********************************/

	DatarouterNodes(){
		this.topLevelNodes = new TreeSet<>();
		this.nodeByName = new TreeMap<>();
		this.topLevelNodesByRouterName = TreeMultimap.create();
		this.routerNameByNode = new TreeMap<>();
		this.clientIdsByRouterName = new TreeMap<>();
		this.physicalNodeByTableNameByClientName = new TreeMap<>();
	}


	/*********************** methods ************************************/

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends Node<PK,D>>
	N register(String routerName, N node){
		ensureDuplicateNamesReferToSameNode(node);
		List<Node<?,?>> nodeWithDescendants = NodeTool.getNodeAndDescendants(node);
		this.topLevelNodes.add(node);
		this.topLevelNodesByRouterName.put(routerName, node);
		for(Node<?,?> nodeOrDescendant : nodeWithDescendants){
			nodeByName.put(nodeOrDescendant.getName(), nodeOrDescendant);
			if(nodeOrDescendant.isPhysicalNodeOrWrapper()){
				PhysicalNode<?, ?> physicalNode = (PhysicalNode<?,?>)nodeOrDescendant;
				String clientName = physicalNode.getClientId().getName();
				String tableName = physicalNode.getTableName();
				physicalNodeByTableNameByClientName.computeIfAbsent(clientName, k -> new TreeMap<>()).put(tableName,
						physicalNode);
			}
			routerNameByNode.put(nodeOrDescendant, routerName);
			clientIdsByRouterName.computeIfAbsent(routerName, k -> new TreeSet<>()).addAll(node.getClientIds());
		}

		return node;
	}

	public Node<?,?> getNode(String nodeName){
		return nodeByName.get(nodeName);
	}

	public List<ClientId> getClientIdsForRouter(String routerName){
		return new ArrayList<>(DrCollectionTool.nullSafe(clientIdsByRouterName.get(routerName)));
	}

	public Set<Class<?>> getTypesForClient(String clientName){
		Set<Class<?>> types = new HashSet<>();
		for(Node<?,?> node : nodeByName.values()){
			if(node.usesClient(clientName)){
				types.add(node.getFieldInfo().getSampleDatabean().getClass());
			}
		}
		return types;
	}

	public Collection<PhysicalNode<?,?>> getPhysicalNodesForClient(String clientName){
		SortedSet<PhysicalNode<?,?>> physicalNodesForClient = new TreeSet<>();
		for(Node<?,?> node : topLevelNodes){
			List<? extends PhysicalNode<?,?>> physicalNodesForNode = node.getPhysicalNodesForClient(clientName);
			for(PhysicalNode<?, ?> physicalNode : physicalNodesForNode){
				physicalNodesForClient.add(physicalNode);
			}
		}
		return physicalNodesForClient;
	}

	public List<String> getTableNamesForClient(String clientName){
		Set<String> tableNames = new TreeSet<>();
		for(PhysicalNode<?,?> physicalNode : getPhysicalNodesForClient(clientName)){
			tableNames.add(physicalNode.getTableName());
		}
		return new ArrayList<>(tableNames);
	}

	public List<String> getTableNamesForRouterAndClient(String routerName, String clientName){
		List<String> tableNames = new ArrayList<>();
		for(PhysicalNode<?,?> physicalNode : getPhysicalNodesForClient(clientName)){
			if(Objects.equals(routerNameByNode.get(physicalNode), routerName)){
				tableNames.add(physicalNode.getTableName());
			}
		}
		return tableNames;
	}

	private void ensureDuplicateNamesReferToSameNode(Node<?,?> node){
		String thisName = node.getName();
		Node<?, ?> existingNode = nodeByName.get(thisName);
		if(existingNode == null || existingNode == node){
			return;
		}
		Class<?> existingNodeClass = existingNode.getClass();
		throw new IllegalArgumentException("different node with this name already exists:"+thisName+"["
				+existingNodeClass.getSimpleName()+"]");
	}

	public PhysicalNode<?,?> getPhyiscalNodeForClientAndTable(String clientName, String tableName){
		return physicalNodeByTableNameByClientName.getOrDefault(clientName, Collections.emptyMap()).get(tableName);
	}


	/*************************** get/set ****************************************/

	public Multimap<String,Node<?,?>> getTopLevelNodesByRouterName(){
		return topLevelNodesByRouterName;
	}

}
