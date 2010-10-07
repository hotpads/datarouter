package com.hotpads.datarouter.node;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.java.ReflectionTool;

public class Nodes<PK extends PrimaryKey<PK>,D extends Databean<PK>,N extends Node<PK,D>>{
	Logger logger = Logger.getLogger(getClass());

	protected List<N> topLevelNodes = ListTool.createArrayList();
	protected List<N> allNodes = ListTool.createArrayList();
	protected List<String> allNames = ListTool.createArrayList();
	protected Map<String,N> nodeByName = MapTool.createTreeMap();
	protected Map<Class<PK>,N> nodeByPrimaryKeyType = MapTool.createHashMap();
	protected Map<Class<D>,N> nodeByDatabeanType = MapTool.createHashMap();
	protected Map<Class<D>,List<String>> clientNamesByDatabeanType = MapTool.createHashMap();
	
	
	public N register(N node){
		String nodeName = node.getName();
//		logger.warn("registering:"+nodeName+":"+node.getAllNames());
		if(CollectionTool.containsAny(this.getAllNames(), node.getAllNames())){//enforce global node name uniqueness
			throw new IllegalArgumentException("node already exists:"+nodeName);
		}
		Class<D> databeanType = node.getDatabeanType();
		D sampleDatabean = ReflectionTool.create(databeanType);
		List<String> clientNames = node.getClientNames();
		@SuppressWarnings("unchecked")
		List<N> nodeWithDescendants = (List<N>)NodeTool.getNodeAndDescendants(node);
		this.topLevelNodes.add(node);
		this.allNodes.addAll(nodeWithDescendants);
		for(N nodeOrDescendant : IterableTool.nullSafe(nodeWithDescendants)){
			this.allNames.add(nodeOrDescendant.getName());
			this.nodeByName.put(nodeOrDescendant.getName(), nodeOrDescendant);
		}
		this.nodeByPrimaryKeyType.put(sampleDatabean.getKeyClass(), node);
		this.nodeByDatabeanType.put(databeanType, node);
		if(this.clientNamesByDatabeanType.get(databeanType)==null){
			this.clientNamesByDatabeanType.put(databeanType, new LinkedList<String>());
		}
		this.clientNamesByDatabeanType.get(databeanType).addAll(clientNames);
		
		Collections.sort(topLevelNodes);
		Collections.sort(allNodes);
		Collections.sort(allNames);
		
		return node;
	}
	
	public List<String> getAllNames(){
		return allNames;
	}
	
	public List<N> getAllNodes(){
		return allNodes;
	}
	
	public List<N> getTopLevelNodes(){
		return topLevelNodes;
	}
	
	public N getNode(String nodeName){
		return nodeByName.get(nodeName);
	}
	
	public Set<Class<D>> getTypesForClient(String clientName){
		Set<Class<D>> types = SetTool.createHashSet();
		for(N node : MapTool.nullSafe(nodeByName).values()){
			if(node.usesClient(clientName)){
				types.add(node.getDatabeanType());
			}
		}
		return types;
	}
	
	public List<? extends PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName){
		List<PhysicalNode<PK,D>> physicalNodesForClient = ListTool.createLinkedList();
		for(N node : IterableTool.nullSafe(topLevelNodes)){
			List<? extends PhysicalNode<PK,D>> physicalNodesForNode = node.getPhysicalNodesForClient(clientName);
			for(PhysicalNode<PK,D> physicalNode : CollectionTool.nullSafe(physicalNodesForNode)){
				physicalNodesForClient.add(physicalNode);
			}
		}
		Collections.sort(physicalNodesForClient);
		return physicalNodesForClient;
	}
	
	public List<String> getTableNamesForClient(String clientName){
		List<? extends PhysicalNode<PK,D>> physicalNodesForClient = getPhysicalNodesForClient(clientName);
		List<String> tableNames = ListTool.create();
		for(PhysicalNode<PK,D> physicalNode : IterableTool.nullSafe(physicalNodesForClient)){
			tableNames.add(physicalNode.getTableName());
		}
		return tableNames;
	}
	
	public N getNode(Key<PK> key){
		return this.nodeByPrimaryKeyType.get(key.getClass());
	}
	
	public N getNode(D databean){
		return this.nodeByDatabeanType.get(databean.getClass());//should just call getNode
	}
	
	public List<String> getClientNamesForKeys(Collection<PK> keys){
		SortedSet<String> clientNames = SetTool.createTreeSet();
		Map<N,LinkedList<PK>> keysByNode = MapTool.createHashMap();
		for(PK key : CollectionTool.nullSafe(keys)){
			N node = this.getNode(key);
			if(keysByNode.get(node)==null){
				keysByNode.put(node, new LinkedList<PK>());
			}
			keysByNode.get(node).add(key);
		}
		for(N node : MapTool.nullSafe(keysByNode).keySet()){
			Collection<String> nodeClientNames = node.getClientNamesForPrimaryKeysForSchemaUpdate(keysByNode.get(node));
			clientNames.addAll(nodeClientNames);
		}
		return ListTool.createArrayList(clientNames);
	}

	public List<String> getClientNamesForDatabeanType(Class<D> databeanType){
		return this.clientNamesByDatabeanType.get(databeanType);
	}
	
	public void clearThreadSpecificState(){
		for(N node : MapTool.nullSafe(nodeByName).values()){
			node.clearThreadSpecificState();
		}
	}
}
