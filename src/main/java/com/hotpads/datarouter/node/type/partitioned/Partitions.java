package com.hotpads.datarouter.node.type.partitioned;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		N extends PhysicalNode<PK,D>> {
	protected static Logger logger = LoggerFactory.getLogger(Partitions.class);

	protected BasePartitionedNode<PK,D,?,N> basePartitionedNode;
	protected List<N> nodes = new ArrayList<>();
	protected Map<String,N> nodeByName = new LinkedHashMap<>();
	protected Map<String,List<String>> nodeNamesByClientName = new HashMap<>();
	protected Map<String,List<String>> clientNamesByNodeName = new HashMap<>();
	protected SortedSet<String> clientNames = new TreeSet<>();
	protected Map<N,Filter<PK>> primaryKeyFilterByNode = new HashMap<>();
	protected Map<N,Filter<D>> databeanFilterByNode = new HashMap<>();
	
	public Partitions(BasePartitionedNode<PK,D,?,N> basePartitionedNode){
		this.basePartitionedNode = basePartitionedNode;
	}
	
	public void add(N node){		
		String nodeName = node.getName();
		if(nodeByName.keySet().contains(nodeName)){//enforce global node name uniqueness
			throw new IllegalArgumentException("node already exists:"+nodeName);
		}
		String clientName = node.getClientName();
		
		//array list
		nodes.add(node);
		
		//nodeByName
		nodeByName.put(nodeName, node);
		
		//nodeNamesByClientName
		if(nodeNamesByClientName.get(clientName)==null){
			nodeNamesByClientName.put(clientName, new LinkedList<String>());
		}
		nodeNamesByClientName.get(clientName).add(nodeName);
		
		//clientNamesByNodeName
		if(clientNamesByNodeName.get(nodeName)==null){
			clientNamesByNodeName.put(nodeName, new LinkedList<String>());
		}
		clientNamesByNodeName.get(nodeName).add(node.getClientName());
		
		clientNames.add(node.getClientName());
		
		primaryKeyFilterByNode.put(node, new PartitionedNodePrimaryKeyFilter<PK,D,N>(basePartitionedNode, node));
		databeanFilterByNode.put(node, new PartitionedNodeDatabeanFilter<PK,D,N>(basePartitionedNode, node));
	}

	public void add(Partitions<PK,D,N> nodes){
		for(N node : nodes.nodeByName.values()){
			add(node);
		}
	}
	
	public N get(int index){
		return nodes.get(index);
	}
	
	public N get(String name){
		return nodeByName.get(name);
	}
	
	public List<N> getAll(){
		return nodes;
	}
	
	public List<N> getPhysicalNodesForClient(String clientName){
		if(clientName==null){
			return getAll();
		}
		List<N> nodes = new LinkedList<>();
		List<String> nodeNames = DrMapTool.nullSafe(nodeNamesByClientName).get(clientName);
		for(String nodeName : DrCollectionTool.nullSafe(nodeNames)){
			nodes.add(nodeByName.get(nodeName));
		}
		return nodes;
	}
	
	public List<Class<D>> getTypesForClient(String clientName){
		Collection<N> nodes = getPhysicalNodesForClient(clientName);
		List<Class<D>> types = new LinkedList<>();
		for(N node : DrCollectionTool.nullSafe(nodes)){
			types.add(node.getDatabeanType());
		}
		return types;
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
