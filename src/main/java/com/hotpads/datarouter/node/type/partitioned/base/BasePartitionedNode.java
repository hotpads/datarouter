package com.hotpads.datarouter.node.type.partitioned.base;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeId;
import com.hotpads.datarouter.node.type.partitioned.Partitions;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.collections.Range;

/*
 * current assumption is that partition can always be determined by the PrimaryKey.  should probably create a
 * new implemenatation in the more obscure case that non-PK fields determine the partition.
 */
public abstract class BasePartitionedNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalNode<PK,D>>
extends BaseNode<PK,D,F>{
	
	//TODO make this class aware of whether we are hosting all data on each partition so it can alternate
	// requests to the underlying nodes in cases where it can't pick a single node

	protected Class<D> databeanClass;
	protected Partitions<PK,D,N> partitions;
		
	public BasePartitionedNode(Class<D> databeanClass, Class<F> fielderClass, DataRouter router){
		super(router, databeanClass, fielderClass);
		this.partitions = new Partitions<PK,D,N>(this);
		this.setId(new NodeId<PK,D,F>((Class<Node<PK,D>>)getClass(), databeanClass, router.getName(), null, null, null));
	}

	/*************************** node methods *************************/

	@Override
	public Set<String> getAllNames(){
		Set<String> names = SetTool.wrap(getName());
		for(N physicalNode : IterableTool.nullSafe(partitions.getAll())){
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
		return partitions.getAll();
	}

	@Override
	public List<String> getClientNames() {
		return partitions.getClientNames();
	}

	@Override
	public boolean usesClient(String clientName){
		return CollectionTool.notEmpty(partitions.getPhysicalNodesForClient(clientName));
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		ArrayListMultimap<N,PK> keysByPhysicalNode = getPrimaryKeysByPhysicalNode(keys);
		List<String> clientNames = ListTool.createLinkedList();
		if(keysByPhysicalNode==null){ return clientNames; }
		for(PhysicalNode<PK,D> node : IterableTool.nullSafe(keysByPhysicalNode.keySet())){
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
		partitions.add(physicalNode);
		return physicalNode;
	}
	
	@Override
	public List<N> getPhysicalNodes() {
		return partitions.getAll();
	}
	
	public N getPhysicalNode(String name){
		return partitions.get(name);
	}
	
	@Override
	public List<N> getPhysicalNodesForClient(String clientName) {
		return partitions.getPhysicalNodesForClient(clientName);
	}
	
	
	/******************* abstract partitioning logic methods ******************/
		
	//for map nodes
	public abstract N getPhysicalNode(PK key);
	
	//for sorted nodes
	public abstract List<N> getPhysicalNodesForFirst();
	public abstract List<N> getPhysicalNodesForRange(Range<PK> range);
	public abstract Multimap<N,PK> getPrefixesByPhysicalNode(Collection<PK> prefixes, boolean wildcardLastField);

	//for indexed nodes
	public abstract boolean isSecondaryKeyPartitionAware(Key<PK> key);
	public abstract List<N> getPhysicalNodesForSecondaryKey(Key<PK> key);
	
	
	/************ common partitioning logic relying on the abstract methods above **********/
	
	public List<N> getPhysicalNodesForSecondaryKeys(Collection<? extends Key<PK>> keys){
		Set<N> nodes = SetTool.createHashSet();
		for(Key<PK> key : IterableTool.nullSafe(keys)){
			nodes.addAll(getPhysicalNodesForSecondaryKey(key));
		}
		return ListTool.createArrayList(nodes);
	}
	
	//used when a physicalNode has keys that don't belong on it.  need to filter them out when they come back
	public List<D> filterDatabeansForPhysicalNode(Collection<D> databeans, N targetNode){
		List<D> filteredDatabeans = ListTool.createArrayList();
		for(D databean : IterableTool.nullSafe(databeans)){
			if(partitions.getPrimaryKeyFilterForNode(targetNode).include(databean.getKey())){
				filteredDatabeans.add(databean);
			}
		}
		return filteredDatabeans;
	}
	
	public ArrayListMultimap<N,PK> getPrimaryKeysByPhysicalNode(Collection<PK> pks){
		ArrayListMultimap<N,PK> primaryKeysByPhysicalNode = ArrayListMultimap.create();
		for(PK pk : IterableTool.nullSafe(pks)){
			N node = getPhysicalNode(pk);
			if(node==null){ continue; }
			primaryKeysByPhysicalNode.put(node, pk);
		}
		return primaryKeysByPhysicalNode;
	}
	
	public ArrayListMultimap<N,D> getDatabeansByPhysicalNode(Collection<D> databeans){
		ArrayListMultimap<N,D> databeansByPhysicalNode = ArrayListMultimap.create();
		for(D databean : IterableTool.nullSafe(databeans)){
			N node = getPhysicalNode(databean.getKey());
			if(node==null){ continue; }
			databeansByPhysicalNode.get(node).add(databean);
		}
		return databeansByPhysicalNode;
	}
}