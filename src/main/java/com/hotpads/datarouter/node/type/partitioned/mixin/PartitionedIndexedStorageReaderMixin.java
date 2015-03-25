package com.hotpads.datarouter.node.type.partitioned.mixin;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader.PhysicalIndexedStorageReaderNode;
import com.hotpads.datarouter.node.type.partitioned.base.BasePartitionedNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;

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


	//warning: i'm not sure what this does or if anything uses it
	@Override
	public Long count(Lookup<PK> lookup, Config config) {
		if(lookup==null){ return null; }
		Long total = 0L;
		Collection<N> nodes = target.getPhysicalNodesForSecondaryKey(lookup);
		//TODO randomize node access to avoid drowning first node
		for(N node : DrIterableTool.nullSafe(nodes)){
			total += node.count(lookup, config);//will count records that aren't supposed to be on the node
		}
		return total;
	}
	
	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config) {
		if(uniqueKey==null){ return null; }
		Collection<N> nodes = target.getPhysicalNodesForSecondaryKey(uniqueKey);
		//TODO randomize node access to avoid drowning first node
		for(N node : DrIterableTool.nullSafe(nodes)){
			D databean = node.lookupUnique(uniqueKey, config);
			if(databean != null){ return databean; }
		}
		return null;
	}

	
	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		if(DrCollectionTool.isEmpty(uniqueKeys)){ return null; }
		Collection<N> nodes = target.getPhysicalNodesForSecondaryKeys(uniqueKeys);
		SortedSet<D> sortedDedupedResults = new TreeSet<>();
		//TODO randomize node access to avoid drowning first node
		for(N node : DrIterableTool.nullSafe(nodes)){
			List<D> singleNodeResults = node.lookupMultiUnique(uniqueKeys, config);
			sortedDedupedResults.addAll(DrCollectionTool.nullSafe(singleNodeResults));
		}
		return DrListTool.createArrayList(sortedDedupedResults);
	}

	
	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config) {
		if(lookup==null){ return null; }
		Collection<N> nodes = target.getPhysicalNodesForSecondaryKey(lookup);
		SortedSet<D> sortedDedupedResults = new TreeSet<>();
		//TODO randomize node access to avoid drowning first node
		for(N node : DrIterableTool.nullSafe(nodes)){
			List<D> singleNodeResults = node.lookup(lookup, wildcardLastField, config);
			sortedDedupedResults.addAll(DrCollectionTool.nullSafe(singleNodeResults));
		}
		return DrListTool.createArrayList(sortedDedupedResults);
	}
	
	
	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config config) {
		if(DrCollectionTool.isEmpty(lookups)){ return null; }
		Collection<N> nodes = target.getPhysicalNodesForSecondaryKeys(lookups);
		SortedSet<D> sortedDedupedResults = new TreeSet<>();
		//TODO randomize node access to avoid drowning first node
		for(N node : DrIterableTool.nullSafe(nodes)){
			for(Lookup<PK> lookup : lookups){
				List<D> singleNodeResults = node.lookup(lookup, false, config);
				sortedDedupedResults.addAll(DrCollectionTool.nullSafe(singleNodeResults));
			}
		}
		return DrListTool.createArrayList(sortedDedupedResults);
	}
	
}
