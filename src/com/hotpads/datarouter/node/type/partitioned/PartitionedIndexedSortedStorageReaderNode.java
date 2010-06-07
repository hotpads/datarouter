package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedStorageReaderNode;
import com.hotpads.datarouter.node.type.physical.PhysicalIndexedSortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public abstract class PartitionedIndexedSortedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK>,
N extends PhysicalIndexedSortedStorageReaderNode<PK,D>>
extends PartitionedSortedStorageReaderNode<PK,D,N>
implements IndexedStorageReaderNode<PK,D>{
	
	public PartitionedIndexedSortedStorageReaderNode(Class<D> databeanClass, DataRouter router) {
		super(databeanClass, router);
	}

	/***************** IndexedStorageReader ************************************/

	/*
	 * MULTIPLE INHERITANCE... copied from: PartitionedIndexedStorageReaderNode
	 */
	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config) {
		if(uniqueKey==null){ return null; }
		Collection<N> nodes = this.getPhysicalNodes(uniqueKey);
		//TODO randomize node access to avoid drowning first node
		for(N node : CollectionTool.nullSafe(nodes)){
			D databean = node.lookupUnique(uniqueKey, config);
			if(databean != null){ return databean; }
		}
		return null;
	}

	/*
	 * MULTIPLE INHERITANCE... copied to: - PartitionedIndexedSortedStorageReaderNode
	 */
	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		if(CollectionTool.isEmpty(uniqueKeys)){ return null; }
		Collection<N> nodes = this.getPhysicalNodes(uniqueKeys);
		//TODO randomize node access to avoid drowning first node
		for(N node : CollectionTool.nullSafe(nodes)){
			List<D> databean = node.lookupMultiUnique(uniqueKeys, config);
			if(databean != null){ return databean; }
		}
		return null;
	}

	/*
	 * MULTIPLE INHERITANCE... copied from: PartitionedIndexedStorageReaderNode
	 */
	@Override
	public List<D> lookup(Lookup<PK> lookup, Config config) {
		if(lookup==null){ return null; }
		List<D> all = ListTool.createLinkedList();
		Collection<N> nodes = this.getPhysicalNodes(lookup);
		for(N node : CollectionTool.nullSafe(nodes)){
			all.addAll(node.lookup(lookup, config));
		}
		return all;
	}

	/*
	 * MULTIPLE INHERITANCE... copied from: PartitionedIndexedStorageReaderNode
	 */
	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config config) {
		if(CollectionTool.isEmpty(lookups)){ return null; }
		List<D> all = ListTool.createLinkedList();
		Collection<N> nodes = this.getPhysicalNodes(lookups);
		for(N node : CollectionTool.nullSafe(nodes)){
			for(Lookup<PK> lookup : lookups){
				all.addAll(node.lookup(lookup, config));
			}
		}
		return all;
	}
	
}
