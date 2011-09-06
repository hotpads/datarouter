package com.hotpads.datarouter.node.type.partitioned.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader.PhysicalIndexedStorageReaderNode;
import com.hotpads.datarouter.node.type.partitioned.base.BasePartitionedNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class PartitionedIndexedStorageReaderMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalIndexedStorageReaderNode<PK,D>>
implements IndexedStorageReader<PK,D>{
	
	protected BasePartitionedNode<PK,D,F,N> target;
	
	public PartitionedIndexedStorageReaderMixin(BasePartitionedNode<PK,D,F,N> target){
		this.target = target;
	}


	@Override
	public Long count(Lookup<PK> lookup, Config config) {
		if(lookup==null){ return null; }
		Long total = 0l;
		Collection<N> nodes = target.getPhysicalNodes(lookup);
		//TODO randomize node access to avoid drowning first node
		for(N node : CollectionTool.nullSafe(nodes)){
			total += node.count(lookup, config);
		}
		return total;
	}
	
	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config) {
		if(uniqueKey==null){ return null; }
		Collection<N> nodes = target.getPhysicalNodes(uniqueKey);
		//TODO randomize node access to avoid drowning first node
		for(N node : CollectionTool.nullSafe(nodes)){
			D databean = node.lookupUnique(uniqueKey, config);
			if(databean != null){ return databean; }
		}
		return null;
	}

	
	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		if(CollectionTool.isEmpty(uniqueKeys)){ return null; }
		Collection<N> nodes = target.getPhysicalNodes(uniqueKeys);
		//TODO randomize node access to avoid drowning first node
		for(N node : CollectionTool.nullSafe(nodes)){
			List<D> databean = node.lookupMultiUnique(uniqueKeys, config);
			if(databean != null){ return databean; }
		}
		return null;
	}

	
	@Override
	public List<D> lookup(Lookup<PK> lookup, Config config) {
		if(lookup==null){ return null; }
		List<D> all = ListTool.createLinkedList();
		Collection<N> nodes = target.getPhysicalNodes(lookup);
		//TODO randomize node access to avoid drowning first node
		for(N node : CollectionTool.nullSafe(nodes)){
			all.addAll(node.lookup(lookup, config));
		}
		return all;
	}
	
	
	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config config) {
		if(CollectionTool.isEmpty(lookups)){ return null; }
		List<D> all = ListTool.createLinkedList();
		Collection<N> nodes = target.getPhysicalNodes(lookups);
		//TODO randomize node access to avoid drowning first node
		for(N node : CollectionTool.nullSafe(nodes)){
			for(Lookup<PK> lookup : lookups){
				all.addAll(node.lookup(lookup, config));
			}
		}
		return all;
	}
	
}
