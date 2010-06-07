package com.hotpads.datarouter.node.base.partitioned;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.base.BaseNode;
import com.hotpads.datarouter.node.base.physical.PhysicalNodes;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;

public abstract class BasePartitionedNode<PK extends PrimaryKey<PK>,D extends Databean<PK>,
		N extends PhysicalNode<PK,D>> 
extends BaseNode<PK,D>{

	protected Class<D> databeanClass;
	protected DataRouter router;
	protected PhysicalNodes<PK,D,N> physicalNodes = new PhysicalNodes<PK,D,N>();
	
	protected String name;
	
	public BasePartitionedNode(Class<D> databeanClass, DataRouter router){
		super(databeanClass);
		this.router = router;
		this.name = databeanClass.getSimpleName()+"."+this.getClass().getSimpleName();
	}

	/*************************** node methods *************************/

	@Override
	public Set<String> getAllNames(){
		Set<String> names = SetTool.wrap(this.name);
		for(N physicalNode : IterableTool.nullSafe(this.physicalNodes.getAll())){
			names.addAll(physicalNode.getAllNames());
		}
		return names;
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return this;
	}

	@Override
	public List<String> getClientNames() {
		return this.physicalNodes.getClientNames();
	}

	@Override
	public boolean usesClient(String clientName){
		return CollectionTool.notEmpty(this.physicalNodes.getPhysicalNodesForClient(clientName));
	}

	@Override
	public List<String> getClientNamesForPrimaryKeys(Collection<PK> keys) {
		Map<N,List<PK>> keysByPhysicalNode = this.getPrimaryKeysByPhysicalNode(keys);
		List<String> clientNames = ListTool.createLinkedList();
		for(PhysicalNode<PK,D> node : MapTool.nullSafe(keysByPhysicalNode).keySet()){
			String clientName = node.getClientName();
			clientNames.add(clientName);
		}
		return clientNames;
	}
	
	@Override
	public void clearThreadSpecificState(){
		//TODO physicalNodes can't even have a cache right now... must be a cleaner way to implement cache invalidation
		for(N physicalNode : this.getPhysicalNodes()){
			physicalNode.clearThreadSpecificState();  
		}
	}
	
	/************************ virtual node methods ***************************/
	
	public N register(N physicalNode){
		this.physicalNodes.add(physicalNode);
		return physicalNode;
	}
	
	@Override
	public List<N> getPhysicalNodes() {
		return this.physicalNodes.getAll();
	}
	
	@Override
	public List<N> getPhysicalNodesForClient(String clientName) {
		return this.physicalNodes.getPhysicalNodesForClient(clientName);
	}

	public abstract boolean isPartitionAware(Key<PK> key);
	
	public abstract List<N> getPhysicalNodes(Key<PK> key);
	
	public List<N> getPhysicalNodes(Collection<? extends Key<PK>> keys){
		Set<N> nodes = SetTool.createHashSet();
		for(Key<PK> key : CollectionTool.nullSafe(keys)){
			nodes.addAll(this.getPhysicalNodes(key));
		}
		return ListTool.createArrayList(nodes);
	}
	
	public Map<N,List<PK>> getPrimaryKeysByPhysicalNode(Collection<PK> keys){
		Map<N,List<PK>> keysByPhysicalNode = MapTool.createHashMap();
		for(PK key : CollectionTool.nullSafe(keys)){
			List<N> nodes = this.getPhysicalNodes(key);
			for(N node : CollectionTool.nullSafe(nodes)){
				if(keysByPhysicalNode.get(node)==null){
					keysByPhysicalNode.put(node, new LinkedList<PK>());
				}
				keysByPhysicalNode.get(node).add(key);
			}
		}
		return keysByPhysicalNode;
	}
	
	public Map<N,List<D>> getDatabeansByPhysicalNode(Collection<D> databeans){
		Map<N,List<D>> databeansByPhysicalNode = MapTool.createHashMap();
		for(D databean : CollectionTool.nullSafe(databeans)){
			List<N> nodes = this.getPhysicalNodes(databean.getKey());
			for(N node : CollectionTool.nullSafe(nodes)){
				if(databeansByPhysicalNode.get(node)==null){
					databeansByPhysicalNode.put(node, new LinkedList<D>());
				}
				databeansByPhysicalNode.get(node).add(databean);
			}
		}
		return databeansByPhysicalNode;
	}
}
