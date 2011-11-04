package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.PhysicalSortedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.node.scanner.MergeScanner;
import com.hotpads.datarouter.node.scanner.primarykey.PrimaryKeyMergeScanner;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.iterable.PeekableIterable;

public abstract class PartitionedSortedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalSortedMapStorageReaderNode<PK,D>>
extends PartitionedMapStorageReaderNode<PK,D,F,N>
implements SortedMapStorageReaderNode<PK,D>{
	
	public PartitionedSortedMapStorageReaderNode(Class<D> databeanClass, Class<F> fielderClass, DataRouter router) {
		super(databeanClass, fielderClass, router);
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
	
	//TODO optimize with merge sort

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
			return all; 
		}
		Collections.sort(all);
		if(config!=null && config.getLimit()!=null && config.getLimit() < all.size()){
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
			return all; 
		}
		Collections.sort(all);
		if(config!=null && config.getLimit()!=null && config.getLimit() < all.size()){
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
			return all; 
		}
		Collections.sort(all);
		if(config!=null && config.getLimit()!=null && config.getLimit() < all.size()){
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
			return all; 
		}
		Collections.sort(all);
		if(config!=null && config.getLimit()!=null && config.getLimit() < all.size()){
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
			return all; 
		}
		Collections.sort(all);
		if(config!=null && config.getLimit()!=null && config.getLimit() < all.size()){
			List<D> limited = ListTool.copyOfRange(all, 0, config.getLimit());
			return limited;
		}else{
			return all;
		}
	}
	
	@Override
	public PeekableIterable<PK> scanKeys(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config){
		List<PeekableIterable<PK>> subScanners = ListTool.createArrayList();
		for(N node : IterableTool.nullSafe(this.getPhysicalNodes())){
			subScanners.add(node.scanKeys(start, startInclusive, end, endInclusive, config));
		}
		return new PrimaryKeyMergeScanner<PK>(subScanners);
	};
	
	@Override
	public PeekableIterable<D> scan(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config){
		List<PeekableIterable<D>> subScanners = ListTool.createArrayList();
		for(N node : IterableTool.nullSafe(this.getPhysicalNodes())){
			subScanners.add(node.scan(start, startInclusive, end, endInclusive, config));
		}
		return new MergeScanner<PK,D>(subScanners);
	};
	
}
