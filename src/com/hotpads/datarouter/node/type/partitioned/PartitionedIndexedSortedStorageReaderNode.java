package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedStorageReaderNode;
import com.hotpads.datarouter.node.type.physical.PhysicalIndexedSortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.index.Lookup;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public abstract class PartitionedIndexedSortedStorageReaderNode<D extends Databean,N extends PhysicalIndexedSortedStorageReaderNode<D>>
extends PartitionedSortedStorageReaderNode<D,N>
implements IndexedStorageReaderNode<D>{
	
	public PartitionedIndexedSortedStorageReaderNode(Class<D> persistentClass, DataRouter router) {
		super(persistentClass, router);
	}

	/***************** IndexedStorageReader ************************************/

	/*
	 * MULTIPLE INHERITANCE... copied from: PartitionedIndexedStorageReaderNode
	 */
	@Override
	public List<D> lookup(Lookup<D> multiKey, Config config) {
		List<D> all = ListTool.createLinkedList();
		Collection<N> nodes = this.getPhysicalNodes(multiKey);
		for(N node : CollectionTool.nullSafe(nodes)){
			all.addAll(node.lookup(multiKey, config));
		}
		return all;
	}
	
}
