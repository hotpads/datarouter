package com.hotpads.datarouter.node.type.partitioned.base;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeId;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.type.partitioned.PartitionedNode;
import com.hotpads.datarouter.node.type.partitioned.Partitions;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrSetTool;
import com.hotpads.util.core.collections.Range;

/*
 * current assumption is that partition can always be determined by the PrimaryKey.  should probably create a
 * new implementation in the more obscure case that non-PK fields determine the partition.
 */
public abstract class BasePartitionedNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalNode<PK,D>>
extends BaseNode<PK,D,F> implements PartitionedNode<PK,D,N>{

	//TODO make this class aware of whether we are hosting all data on each partition so it can alternate
	// requests to the underlying nodes in cases where it can't pick a single node

	protected Partitions<PK,D,N> partitions;

	public BasePartitionedNode(Supplier<D> databeanSupplier, Supplier<F> fielderSupplier, Router router){
		super(new NodeParamsBuilder<PK,D,F>(router, databeanSupplier)
				.withFielder(fielderSupplier)
				.build());
		this.partitions = new Partitions<>(this);
		this.setId(new NodeId<PK,D,F>(getClass().getSimpleName(), databeanSupplier.get().getDatabeanName(),
				router.getName(), null, null, null));
	}

	/*************************** node methods *************************/

	@Override
	public Set<String> getAllNames(){
		Set<String> names = DrSetTool.wrap(getName());
		for(N physicalNode : DrIterableTool.nullSafe(partitions.getAllNodes())){
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
		return partitions.getAllNodes();
	}

	@Override
	public List<String> getClientNames() {
		return partitions.getClientNames();
	}

	@Override
	public List<ClientId> getClientIds(){
		return partitions.getClientIds();
	}

	@Override
	public boolean usesClient(String clientName){
		return DrCollectionTool.notEmpty(partitions.getPhysicalNodesForClient(clientName));
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		ArrayListMultimap<N,PK> keysByPhysicalNode = getPrimaryKeysByPhysicalNode(keys);
		List<String> clientNames = new LinkedList<>();
		if(keysByPhysicalNode==null){
			return clientNames;
		}
		for(PhysicalNode<PK,D> node : DrIterableTool.nullSafe(keysByPhysicalNode.keySet())){
			String clientName = node.getClientId().getName();
			clientNames.add(clientName);
		}
		return clientNames;
	}

	/************************ virtual node methods ***************************/

	public N register(N physicalNode){
		partitions.addNode(physicalNode);
		return physicalNode;
	}

	@Override
	public List<N> getPhysicalNodes() {
		return partitions.getAllNodes();
	}

	@Override
	public List<N> getPhysicalNodesForClient(String clientName) {
		return partitions.getPhysicalNodesForClient(clientName);
	}


	/******************* abstract partitioning logic methods ******************/

	//for sorted nodes
	public abstract List<N> getPhysicalNodesForRange(Range<PK> range);
	public abstract Multimap<N,PK> getPrefixesByPhysicalNode(Collection<PK> prefixes, boolean wildcardLastField);

	/************ common partitioning logic relying on the abstract methods above **********/

	public Set<N> getPhysicalNodesForRanges(Collection<Range<PK>> ranges){
		return ranges.stream().map(this::getPhysicalNodesForRange).flatMap(List::stream).collect(Collectors.toSet());
	}

	@Override
	public <IK extends Key<?>> List<N> getPhysicalNodesForSecondaryKeys(Collection<IK> keys){
		Set<N> nodes = new HashSet<>();
		for(IK key : DrIterableTool.nullSafe(keys)){
			nodes.addAll(getPhysicalNodesForSecondaryKey(key));
		}
		return DrListTool.createArrayList(nodes);
	}

	@Override
	public ArrayListMultimap<N,PK> getPrimaryKeysByPhysicalNode(Collection<PK> pks){
		ArrayListMultimap<N,PK> primaryKeysByPhysicalNode = ArrayListMultimap.create();
		for(PK pk : DrIterableTool.nullSafe(pks)){
			N node = getPhysicalNode(pk);
			if(node==null){
				continue;
			}
			primaryKeysByPhysicalNode.put(node, pk);
		}
		return primaryKeysByPhysicalNode;
	}

	@Override
	public ArrayListMultimap<N,D> getDatabeansByPhysicalNode(Collection<D> databeans){
		ArrayListMultimap<N,D> databeansByPhysicalNode = ArrayListMultimap.create();
		for(D databean : DrIterableTool.nullSafe(databeans)){
			N node = getPhysicalNode(databean.getKey());
			if(node==null){
				continue;
			}
			databeansByPhysicalNode.get(node).add(databean);
		}
		return databeansByPhysicalNode;
	}
}
