package com.hotpads.datarouter.node.base.physical;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;

public class PhysicalNodes<D extends Databean,PK extends PrimaryKey<D>,N extends PhysicalNode<D,PK>> {
	Logger logger = Logger.getLogger(getClass());

	List<N> nodes = ListTool.createArrayList();
	Map<String,N> nodeByName = MapTool.createLinkedHashMap();
	Map<String,List<String>> nodeNamesByClientName = MapTool.createHashMap();
	Map<String,List<String>> clientNamesByNodeName = MapTool.createHashMap();
	SortedSet<String> clientNames = SetTool.createTreeSet();
	
	public PhysicalNodes(){
	}
	
	public void add(N node){		
		String nodeName = node.getName();
		String clientName = node.getClientName();
		
		//array list
		this.nodes.add(node);
		
		//nodeByName
		this.nodeByName.put(nodeName, node);
		
		//nodeNamesByClientName
		if(this.nodeNamesByClientName.get(clientName)==null){
			this.nodeNamesByClientName.put(clientName, new LinkedList<String>());
		}
		this.nodeNamesByClientName.get(clientName).add(nodeName);
		
		//clientNamesByNodeName
		if(this.clientNamesByNodeName.get(nodeName)==null){
			this.clientNamesByNodeName.put(nodeName, new LinkedList<String>());
		}
		this.clientNamesByNodeName.get(nodeName).add(node.getClientName());
		
		this.clientNames.add(node.getClientName());
	}

	public void add(PhysicalNodes<D,PK,N> nodes){
		for(N node : nodes.nodeByName.values()){
			this.add(node);
		}
	}
	
	public N get(int index){
		return this.nodes.get(index);
	}
	
	public N get(String name){
		return this.nodeByName.get(name);
	}
	
	public List<N> getAll(){
		return this.nodes;
	}
	
	public List<N> getPhysicalNodesForClient(String clientName){
		if(clientName==null){
			return getAll();
		}
		List<N> nodes = ListTool.createLinkedList();
		List<String> nodeNames = MapTool.nullSafe(this.nodeNamesByClientName).get(clientName);
		for(String nodeName : CollectionTool.nullSafe(nodeNames)){
			nodes.add(this.nodeByName.get(nodeName));
		}
		return nodes;
	}
	
	public List<Class<D>> getTypesForClient(String clientName){
		Collection<N> nodes = getPhysicalNodesForClient(clientName);
		List<Class<D>> types = ListTool.createLinkedList();
		for(N node : CollectionTool.nullSafe(nodes)){
			types.add(node.getDatabeanType());
		}
		return types;
	}
	
	public List<String> getClientNames(){
		return ListTool.createArrayList(this.clientNames);
	}
}
