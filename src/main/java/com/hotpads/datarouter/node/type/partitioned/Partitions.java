package com.hotpads.datarouter.node.type.partitioned;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hotpads.datarouter.node.type.partitioned.base.BasePartitionedNode;
import com.hotpads.datarouter.node.type.partitioned.filter.PartitionedNodeDatabeanFilter;
import com.hotpads.datarouter.node.type.partitioned.filter.PartitionedNodePrimaryKeyFilter;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.util.core.iterable.scanner.filter.Filter;

public class Partitions<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalNode<PK,D>>{

	private final BasePartitionedNode<PK,D,?,N> basePartitionedNode;//ugly circular reference
	private final SortedSet<String> clientNames;
	private final List<N> nodes;
	private final Map<String,N> nodeByName;
	private final Map<String,List<String>> nodeNamesByClientName;
	private final Map<String,List<String>> clientNamesByNodeName;
	private final Map<N,Filter<PK>> primaryKeyFilterByNode;
	private final Map<N,Filter<D>> databeanFilterByNode;
	
	public Partitions(BasePartitionedNode<PK,D,?,N> basePartitionedNode){
		this.basePartitionedNode = basePartitionedNode;
		this.clientNames = new TreeSet<>();
		this.nodes = new ArrayList<>();
		this.nodeByName = new LinkedHashMap<>();
		this.nodeNamesByClientName = new HashMap<>();
		this.clientNamesByNodeName = new HashMap<>();
		this.primaryKeyFilterByNode = new HashMap<>();
		this.databeanFilterByNode = new HashMap<>();
	}
	
	public void addNode(N node){		
		String nodeName = node.getName();
		if(nodeByName.keySet().contains(nodeName)){//enforce global node name uniqueness
			throw new IllegalArgumentException("node already exists:"+nodeName);
		}
		String clientName = node.getClientName();
		clientNames.add(clientName);
		
		//array list
		nodes.add(node);
		
		//nodeByName
		nodeByName.put(nodeName, node);
		
		//nodeNamesByClientName
		if(nodeNamesByClientName.get(clientName)==null){
			nodeNamesByClientName.put(clientName, new ArrayList<String>());
		}
		nodeNamesByClientName.get(clientName).add(nodeName);
		
		//clientNamesByNodeName
		if(clientNamesByNodeName.get(nodeName)==null){
			clientNamesByNodeName.put(nodeName, new ArrayList<String>());
		}
		clientNamesByNodeName.get(nodeName).add(clientName);
		
		primaryKeyFilterByNode.put(node, new PartitionedNodePrimaryKeyFilter<PK,D,N>(basePartitionedNode, node));
		databeanFilterByNode.put(node, new PartitionedNodeDatabeanFilter<PK,D,N>(basePartitionedNode, node));
	}

	public void addNodes(Partitions<PK,D,N> nodes){
		for(N node : nodes.nodeByName.values()){
			addNode(node);
		}
	}
	
	public N getNodeAtIndex(int index){
		return nodes.get(index);
	}
	
	public List<N> getAllNodes(){
		return nodes;
	}
	
	public List<N> getPhysicalNodesForClient(String clientName){
		if(clientName==null){
			return getAllNodes();
		}
		List<N> nodes = new ArrayList<>();
		List<String> nodeNames = DrMapTool.nullSafe(nodeNamesByClientName).get(clientName);
		for(String nodeName : DrCollectionTool.nullSafe(nodeNames)){
			nodes.add(nodeByName.get(nodeName));
		}
		return nodes;
	}
	
	public List<String> getClientNames(){
		return DrListTool.createArrayList(clientNames);
	}
	
	public Filter<PK> getPrimaryKeyFilterForNode(N node){
		return primaryKeyFilterByNode.get(node);
	}
	
	public Filter<D> getDatabeanFilterForNode(N node){
		return databeanFilterByNode.get(node);
	}
}
