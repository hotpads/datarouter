package com.hotpads.datarouter.node.base.partitioned;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.base.physical.PhysicalNodes;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.op.MapStorageReadOps;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public abstract class BasePartitionedNode<D extends Databean,N extends PhysicalNode<D>> 
implements Node<D>{

	protected Class<D> persistentClass;
	protected DataRouter router;
	protected PhysicalNodes<D,N> physicalNodes = new PhysicalNodes<D,N>();
	
	protected String name;
	
	public BasePartitionedNode(Class<D> persistentClass, DataRouter router){
		this.persistentClass = persistentClass;
		this.router = router;
		this.name = this.getClass().getSimpleName();
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
	public Node<D> getMaster() {
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
	public <K extends Key<D>> List<String> getClientNamesForKeys(Collection<K> keys) {
		Map<N,List<Key<D>>> keysByPhysicalNode = this.getKeysByPhysicalNode(keys);
		List<String> clientNames = ListTool.createLinkedList();
		for(PhysicalNode<D> node : MapTool.nullSafe(keysByPhysicalNode).keySet()){
			String clientName = node.getClientName();
			clientNames.add(clientName);
		}
		return clientNames;
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

	public abstract boolean isPartitionAware(Key<D> key);
	
	public abstract List<N> getPhysicalNodes(Key<D> key);
	
	public Map<N,List<Key<D>>> getKeysByPhysicalNode(Collection<? extends Key<D>> keys){
		Map<N,List<Key<D>>> keysByPhysicalNode = MapTool.createHashMap();
		for(Key<D> key : CollectionTool.nullSafe(keys)){
			List<N> nodes = this.getPhysicalNodes(key);
			for(N node : CollectionTool.nullSafe(nodes)){
				if(keysByPhysicalNode.get(node)==null){
					keysByPhysicalNode.put(node, new LinkedList<Key<D>>());
				}
				keysByPhysicalNode.get(node).add(key);
			}
		}
		return keysByPhysicalNode;
	}
	
	@SuppressWarnings("unchecked")
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
