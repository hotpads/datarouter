package com.hotpads.datarouter.node;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;

public class Nodes<D extends Databean,K extends Key<D>,N extends Node<D>>{
	Logger logger = Logger.getLogger(getClass());

	protected Map<String,N> nodeByName = MapTool.createHashMap();
	protected Map<Class<D>,N> nodeByDatabeanType = MapTool.createHashMap();
	protected Map<Class<D>,List<String>> clientNamesByDatabeanType = MapTool.createHashMap();
	
	
	public N register(N node){
		String nodeName = node.getName();
		Class<D> databeanType = node.getDatabeanType();
		List<String> clientNames = node.getClientNames();
		this.nodeByName.put(nodeName, node);
		this.nodeByDatabeanType.put(databeanType, node);
		if(this.clientNamesByDatabeanType.get(databeanType)==null){
			this.clientNamesByDatabeanType.put(databeanType, new LinkedList<String>());
		}
		this.clientNamesByDatabeanType.get(databeanType).addAll(clientNames);
		return node;
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
	
	public List<? extends PhysicalNode<D>> getPhysicalNodesForClient(String clientName){
		List<PhysicalNode<D>> physicalNodesForClient = ListTool.createLinkedList();
		for(N node : MapTool.nullSafe(nodeByName).values()){
			List<? extends PhysicalNode<D>> physicalNodesForNode = node.getPhysicalNodesForClient(clientName);
			for(PhysicalNode<D> physicalNode : CollectionTool.nullSafe(physicalNodesForNode)){
//				if(physicalNode.usesClient(clientName)){  //nodes do the filtering now
					physicalNodesForClient.add(physicalNode);
//				}
			}
		}
		return physicalNodesForClient;
	}
	
	public N getNode(Key<D> key){
		return this.nodeByDatabeanType.get(key.getDatabeanClass());
	}
	
	public N getNode(D databean){
		return this.nodeByDatabeanType.get(databean.getClass());
	}
	
	public List<String> getClientNamesForKeys(Collection<Key<D>> keys){
		SortedSet<String> clientNames = SetTool.createTreeSet();
		Map<N,LinkedList<Key<D>>> keysByNode = MapTool.createHashMap();
		for(Key<D> key : CollectionTool.nullSafe(keys)){
			N node = this.getNode(key);
			if(keysByNode.get(node)==null){
				keysByNode.put(node, new LinkedList<Key<D>>());
			}
			keysByNode.get(node).add(key);
		}
		for(N node : MapTool.nullSafe(keysByNode).keySet()){
			Collection<String> nodeClientNames = node.getClientNamesForKeys(keysByNode.get(node));
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
