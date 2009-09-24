package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.SortedStorageReaderNode;
import com.hotpads.datarouter.node.type.physical.PhysicalSortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public abstract class PartitionedSortedStorageReaderNode<D extends Databean,N extends PhysicalSortedStorageReaderNode<D>>
extends PartitionedMapStorageReaderNode<D,N>
implements SortedStorageReaderNode<D>{
	
	public PartitionedSortedStorageReaderNode(Class<D> persistentClass, DataRouter router) {
		super(persistentClass, router);
	}

	/************************* sorted storage methods *****************************/
	
	@Override
	public D getFirst(Config config) {
		SortedSet<D> firstFromEachNode = SetTool.createTreeSet();
		for(N node : CollectionTool.nullSafe(getPhysicalNodes())){
			D databean = node.getFirst(config);
			firstFromEachNode.add(databean);
		}
		return CollectionTool.getFirst(firstFromEachNode);
	}

	@Override
	public List<D> getRange(final Key<D> start, final boolean startInclusive, 
			final Key<D> end, final boolean endInclusive, final Config config) {
		//TODO smarter/optional sorting
		List<D> all = ListTool.createArrayList();
		for(N node : CollectionTool.nullSafe(getPhysicalNodes())){
			all.addAll(node.getRange(start, startInclusive, end, endInclusive, config));
		}
		if(CollectionTool.isEmpty(all)){ 
			return null; 
		}
		Collections.sort(all);
		if(config!=null && config.getLimit()!=null && config.getLimit() >= all.size()){
			List<D> limited = ListTool.copyOfRange(all, 0, config.getLimit());
			return limited;
		}else{
			return all;
		}
	}

	@Override
	public List<D> getRangeWithPrefix(Key<D> prefix, boolean wildcardLastField, Config config) {
		//TODO smarter/optional sorting
		List<D> all = ListTool.createArrayList();
		for(N node : CollectionTool.nullSafe(getPhysicalNodes())){
			all.addAll(node.getRangeWithPrefix(prefix, wildcardLastField, config));
		}
		if(CollectionTool.isEmpty(all)){ 
			return null; 
		}
		Collections.sort(all);
		if(config!=null && config.getLimit()!=null && config.getLimit() >= all.size()){
			List<D> limited = ListTool.copyOfRange(all, 0, config.getLimit());
			return limited;
		}else{
			return all;
		}
	}

	
	
}
