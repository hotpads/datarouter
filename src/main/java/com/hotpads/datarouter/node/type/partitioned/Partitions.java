package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.node.type.partitioned.base.BasePartitionedNode;
import com.hotpads.datarouter.node.type.partitioned.filter.PartitionedNodeDatabeanFilter;
import com.hotpads.datarouter.node.type.partitioned.filter.PartitionedNodePrimaryKeyFilter;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.CollectionTool;
import com.hotpads.datarouter.util.core.ListTool;
import com.hotpads.datarouter.util.core.MapTool;
import com.hotpads.datarouter.util.core.SetTool;
import com.hotpads.util.core.iterable.scanner.filter.Filter;

public class Partitions<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalNode<PK,D>> {
	protected static Logger logger = LoggerFactory.getLogger(Partitions.class);

	protected BasePartitionedNode<PK,D,?,N> basePartitionedNode;
	protected List<N> nodes = ListTool.createArrayList();
	protected Map<String,N> nodeByName = MapTool.createLinkedHashMap();
	protected Map<String,List<String>> nodeNamesByClientName = MapTool.createHashMap();
	protected Map<String,List<String>> clientNamesByNodeName = MapTool.createHashMap();
	protected SortedSet<String> clientNames = SetTool.createTreeSet();
	protected Map<N,Filter<PK>> primaryKeyFilterByNode = MapTool.createHashMap();
	protected Map<N,Filter<D>> databeanFilterByNode = MapTool.createHashMap();
	
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
		List<N> nodes = ListTool.createLinkedList();
		List<String> nodeNames = MapTool.nullSafe(nodeNamesByClientName).get(clientName);
		for(String nodeName : CollectionTool.nullSafe(nodeNames)){
			nodes.add(nodeByName.get(nodeName));
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
		return ListTool.createArrayList(clientNames);
	}
	
	public Filter<PK> getPrimaryKeyFilterForNode(N node){
		return primaryKeyFilterByNode.get(node);
	}
	
	public Filter<D> getDatabeanFilterForNode(N node){
		return databeanFilterByNode.get(node);
	}
}
