package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.SortedStorageReaderNode;
import com.hotpads.datarouter.node.type.physical.PhysicalSortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public abstract class PartitionedSortedStorageReaderNode<D extends Databean,
PK extends PrimaryKey<D>,N extends PhysicalSortedStorageReaderNode<D,PK>>
extends PartitionedMapStorageReaderNode<D,PK,N>
implements SortedStorageReaderNode<D,PK>{
	
	public PartitionedSortedStorageReaderNode(Class<D> persistentClass, DataRouter router) {
		super(persistentClass, router);
	}

	/************************* sorted storage methods *****************************/
	
	@Override
	public D getFirst(Config config) {
		SortedSet<D> firstFromEachNode = SetTool.createTreeSet();
		for(N node : CollectionTool.nullSafe(getPhysicalNodes())){
			D databean = node.getFirst(config);
			if(databean==null){ continue; }
			firstFromEachNode.add(databean);
		}
		return CollectionTool.getFirst(firstFromEachNode);
	}

	@Override
	public PK getFirstKey(Config config) {
		SortedSet<PK> firstFromEachNode = SetTool.createTreeSet();
		for(N node : CollectionTool.nullSafe(getPhysicalNodes())){
			PK key = node.getFirstKey(config);
			if(key==null){ continue; }
			firstFromEachNode.add(key);
		}
		return CollectionTool.getFirst(firstFromEachNode);
	}

	@Override
	public List<D> getPrefixedRange(PK prefix, boolean wildcardLastField, 
			final PK start, final boolean startInclusive,
			Config config) {
		//TODO smarter/optional sorting
		List<D> all = ListTool.createArrayList();
		for(N node : CollectionTool.nullSafe(this.getPhysicalNodes(prefix))){
			all.addAll(node.getPrefixedRange(
					prefix, wildcardLastField, 
					start, startInclusive,
					config));
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
	public List<PK> getKeysInRange(final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, final Config config) {
		//TODO smarter/optional sorting
		List<PK> all = ListTool.createArrayList();
		for(N node : CollectionTool.nullSafe(this.getPhysicalNodes())){
			all.addAll(node.getKeysInRange(start, startInclusive, end, endInclusive, config));
		}
		if(CollectionTool.isEmpty(all)){ 
			return null; 
		}
		Collections.sort(all);
		if(config!=null && config.getLimit()!=null && config.getLimit() >= all.size()){
			List<PK> limited = ListTool.copyOfRange(all, 0, config.getLimit());
			return limited;
		}else{
			return all;
		}
	}

	@Override
	public List<D> getRange(final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, final Config config) {
		//TODO smarter/optional sorting
		List<D> all = ListTool.createArrayList();
		for(N node : CollectionTool.nullSafe(this.getPhysicalNodes())){
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
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		//TODO smarter/optional sorting
		List<D> all = ListTool.createArrayList();
		for(N node : CollectionTool.nullSafe(this.getPhysicalNodes(prefix))){
			all.addAll(node.getWithPrefix(prefix, wildcardLastField, config));
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
	public List<D> getWithPrefixes(Collection<? extends PK> prefixes, boolean wildcardLastField, Config config) {
		//TODO smarter/optional sorting
		List<D> all = ListTool.createArrayList();
		for(N node : CollectionTool.nullSafe(this.getPhysicalNodes(prefixes))){
			//TODO don't send all keys to all nodes in the search
			all.addAll(node.getWithPrefixes(prefixes, wildcardLastField, config));
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
