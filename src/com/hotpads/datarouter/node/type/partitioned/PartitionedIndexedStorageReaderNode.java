package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedStorageReaderNode;
import com.hotpads.datarouter.node.type.physical.PhysicalIndexedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.index.Lookup;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public abstract class PartitionedIndexedStorageReaderNode<D extends Databean,N extends PhysicalIndexedStorageReaderNode<D>>
extends PartitionedMapStorageReaderNode<D,N>
implements IndexedStorageReaderNode<D>{
	
	public PartitionedIndexedStorageReaderNode(Class<D> persistentClass, DataRouter router) {
		super(persistentClass, router);
	}

	/***************** IndexedStorageReader ************************************/

	/*
	 * MULTIPLE INHERITANCE... copied to:
	 *   - PartitionedIndexedSortedStorageReaderNode
	 */
	@Override
	public List<D> lookup(Lookup<D> lookup, Config config) {
		if(lookup==null){ return null; }
		List<D> all = ListTool.createLinkedList();
		Collection<N> nodes = this.getPhysicalNodes(lookup);
		for(N node : CollectionTool.nullSafe(nodes)){
			all.addAll(node.lookup(lookup, config));
		}
		return all;
	}
	
	/*
	 * MULTIPLE INHERITANCE... copied to:
	 *   - PartitionedIndexedSortedStorageReaderNode
	 */
	@Override
	public List<D> lookup(Collection<? extends Lookup<D>> lookups, Config config) {
		if(CollectionTool.isEmpty(lookups)){ return null; }
		List<D> all = ListTool.createLinkedList();
		Collection<N> nodes = this.getPhysicalNodes(lookups);
		for(N node : CollectionTool.nullSafe(nodes)){
			for(Lookup<D> lookup : lookups){
				all.addAll(node.lookup(lookup, config));
			}
		}
		return all;
	}
	
}
