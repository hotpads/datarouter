package com.hotpads.datarouter.node.base.partitioned;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.base.physical.PhysicalNodes;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;

public abstract class BasePartitionedNode<D extends Databean<PK>,PK extends PrimaryKey<PK>,
		N extends PhysicalNode<D,PK>> 
implements Node<D,PK>{

	protected Class<D> persistentClass;
	protected DataRouter router;
	protected PhysicalNodes<D,PK,N> physicalNodes = new PhysicalNodes<D,PK,N>();
	
	protected String name;
	
	public BasePartitionedNode(Class<D> persistentClass, DataRouter router){
		this.persistentClass = persistentClass;
		this.router = router;
		this.name = persistentClass.getSimpleName()+"."+this.getClass().getSimpleName();
	}

	/*************************** node methods *************************/
	
	@Override
	public Class<D> getDatabeanType() {
		return this.persistentClass;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public Node<D,PK> getMaster() {
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
	public <K extends UniqueKey<PK>> List<String> getClientNamesForKeys(Collection<K> keys) {
		Map<N,List<UniqueKey<PK>>> keysByPhysicalNode = this.getKeysByPhysicalNode(keys);
		List<String> clientNames = ListTool.createLinkedList();
		for(PhysicalNode<D,PK> node : MapTool.nullSafe(keysByPhysicalNode).keySet()){
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
	
	public Map<N,List<UniqueKey<PK>>> getKeysByPhysicalNode(Collection<? extends UniqueKey<PK>> keys){
		Map<N,List<UniqueKey<PK>>> keysByPhysicalNode = MapTool.createHashMap();
		for(UniqueKey<PK> key : CollectionTool.nullSafe(keys)){
			List<N> nodes = this.getPhysicalNodes(key);
			for(N node : CollectionTool.nullSafe(nodes)){
				if(keysByPhysicalNode.get(node)==null){
					keysByPhysicalNode.put(node, new LinkedList<UniqueKey<PK>>());
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
