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
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.java.ReflectionTool;

public class Nodes<PK extends PrimaryKey<PK>,D extends Databean<PK>,N extends Node<PK,D>>{
	Logger logger = Logger.getLogger(getClass());

	protected Map<String,N> nodeByName = MapTool.createHashMap();
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
		this.nodeByName.put(nodeName, node);
		this.nodeByPrimaryKeyType.put(sampleDatabean.getKeyClass(), node);
		this.nodeByDatabeanType.put(databeanType, node);
		if(this.clientNamesByDatabeanType.get(databeanType)==null){
			this.clientNamesByDatabeanType.put(databeanType, new LinkedList<String>());
		}
		this.clientNamesByDatabeanType.get(databeanType).addAll(clientNames);
		return node;
	}
	
	public Set<String> getAllNames(){
		Set<String> names = SetTool.createHashSet();
		for(Map.Entry<String,N> entry : MapTool.nullSafe(this.nodeByName).entrySet()){
			names.addAll(entry.getValue().getAllNames());
		}
		return names;
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
		for(N node : MapTool.nullSafe(nodeByName).values()){
			List<? extends PhysicalNode<PK,D>> physicalNodesForNode = node.getPhysicalNodesForClient(clientName);
			for(PhysicalNode<PK,D> physicalNode : CollectionTool.nullSafe(physicalNodesForNode)){
//				if(physicalNode.usesClient(clientName)){  //nodes do the filtering now
					physicalNodesForClient.add(physicalNode);
//				}
			}
		}
		return physicalNodesForClient;
	}
	
	public N getNode(Key<PK> key){
		return this.nodeByPrimaryKeyType.get(key.getClass());
	}
	
	public N getNode(D databean){
		return this.nodeByDatabeanType.get(databean.getClass());
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
			Collection<String> nodeClientNames = node.getClientNamesForPrimaryKeys(keysByNode.get(node));
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
