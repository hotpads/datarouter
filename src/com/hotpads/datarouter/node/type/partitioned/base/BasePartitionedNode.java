package com.hotpads.datarouter.node.type.partitioned.base;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.SortedSetMultimap;
import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.node.type.physical.base.PhysicalNodes;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.collections.Range;

public abstract class BasePartitionedNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalNode<PK,D>>
extends BaseNode<PK,D,F>{
	
	//TODO make this class aware of whether we are hosting all data on each partition so it can alternate
	// requests to the underlying nodes in cases where it can't pick a single node

	protected Class<D> databeanClass;
	protected DataRouter router;
	protected PhysicalNodes<PK,D,N> physicalNodes = new PhysicalNodes<PK,D,N>();
		
	public BasePartitionedNode(Class<D> databeanClass, Class<F> fielderClass, DataRouter router){
		super(databeanClass, fielderClass);
		this.router = router;
		this.name = databeanClass.getSimpleName()+"."+getClass().getSimpleName();
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
	public List<? extends Node<PK,D>> getChildNodes(){
		return physicalNodes.getAll();
	}

	@Override
	public List<String> getClientNames() {
		return physicalNodes.getClientNames();
	}

	@Override
	public boolean usesClient(String clientName){
		return CollectionTool.notEmpty(physicalNodes.getPhysicalNodesForClient(clientName));
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		Map<N,List<PK>> keysByPhysicalNode = getPrimaryKeysByPhysicalNode(keys);
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
		for(N physicalNode : getPhysicalNodes()){
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
	
	public N getPhysicalNode(String name){
		return this.physicalNodes.get(name);
	}
	
	@Override
	public List<N> getPhysicalNodesForClient(String clientName) {
		return this.physicalNodes.getPhysicalNodesForClient(clientName);
	}
	
	
	/******************* abstract partitioning logic methods ******************/

	public abstract boolean isPartitionAware(Key<PK> key);
	
	public abstract List<N> getPhysicalNodes(Key<PK> key);
	
	public abstract List<N> getPhysicalNodesForRange(Range<PK> range);
	
	public abstract SortedSetMultimap<N,PK>	getPrefixesByPhysicalNode(Collection<PK> prefixes, 
			boolean wildcardLastField);
	
	
	/************ common partitioning logic relying on the abstract methods above **********/
	
	public List<N> getPhysicalNodes(Collection<? extends Key<PK>> keys){
		Set<N> nodes = SetTool.createHashSet();
		for(Key<PK> key : CollectionTool.nullSafe(keys)){
			nodes.addAll(getPhysicalNodes(key));
		}
		return ListTool.createArrayList(nodes);
	}
	
	//used when a physicalNode has keys that don't belong on it.  need to filter them out when they come back
	public List<PK> filterPrimaryKeysForPhysicalNode(Collection<PK> keys, N node){
		List<PK> filteredPks = ListTool.createArrayList();
		for(PK key : CollectionTool.nullSafe(keys)){
			List<N> nodes = getPhysicalNodes(key);
			if(nodes.contains(node)){
				filteredPks.add(key);
			}
		}
		return filteredPks;
	}
	
	//used when a physicalNode has keys that don't belong on it.  need to filter them out when they come back
	public List<D> filterDatabeansForPhysicalNode(Collection<D> databeans, N node){
		List<D> filteredDatabeans = ListTool.createArrayList();
		for(D databean : CollectionTool.nullSafe(databeans)){
			List<N> nodes = getPhysicalNodes(databean.getKey());
			if(nodes.contains(node)){
				filteredDatabeans.add(databean);
			}
		}
		return filteredDatabeans;
	}
	
	public Map<N,List<PK>> getPrimaryKeysByPhysicalNode(Collection<PK> keys){
		Map<N,List<PK>> keysByPhysicalNode = MapTool.createHashMap();
		for(PK key : CollectionTool.nullSafe(keys)){
			List<N> nodes = getPhysicalNodes(key);
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
			List<N> nodes = getPhysicalNodes(databean.getKey());
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
