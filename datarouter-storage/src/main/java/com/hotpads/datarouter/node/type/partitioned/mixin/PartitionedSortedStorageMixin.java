package com.hotpads.datarouter.node.type.partitioned.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Multimap;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage.PhysicalSortedStorageNode;
import com.hotpads.datarouter.node.type.partitioned.PartitionedNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.Scanner;
import com.hotpads.util.core.iterable.scanner.collate.PriorityQueueCollator;
import com.hotpads.util.core.iterable.scanner.filter.Filter;
import com.hotpads.util.core.iterable.scanner.filter.FilteringSortedScanner;
import com.hotpads.util.core.iterable.scanner.iterable.IteratorScanner;
import com.hotpads.util.core.iterable.scanner.iterable.SingleUseScannerIterable;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

public interface PartitionedSortedStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalSortedStorageNode<PK,D>>
extends SortedStorage<PK,D>, PartitionedNode<PK,D,N>{

	@Override
	default List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		return getWithPrefixes(DrListTool.wrap(prefix), wildcardLastField, config);
	}

	@Override
	@Deprecated
	default List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config){
		List<D> all = new ArrayList<>();
		Multimap<N,PK> prefixesByNode = getPrefixesByPhysicalNode(prefixes, wildcardLastField);
		for(N node : prefixesByNode.keySet()){
			Collection<PK> prefixesForNode = prefixesByNode.get(node);
			all.addAll(node.getWithPrefixes(prefixesForNode, wildcardLastField, config));
		}
		if(DrCollectionTool.isEmpty(all)){
			return all;
		}
		Collections.sort(all);
		return getLimitedCopyOfResultIfNecessary(config, all);
	}

	// TODO add option to the BasePartitionedNode to skip filtering when not needed
	@Override
	default SingleUseScannerIterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		List<SortedScanner<PK>> subScanners = new ArrayList<>();
		for(N node : getPhysicalNodesForRanges(ranges)){
			Scanner<PK> scanner = new IteratorScanner<>(node.scanKeysMulti(ranges, config).iterator());
			Filter<PK> filter = getPartitions().getPrimaryKeyFilterForNode(node);
			FilteringSortedScanner<PK> filteredScanner = new FilteringSortedScanner<>(scanner, filter);
			subScanners.add(filteredScanner);
		}
		SortedScanner<PK> collator = new PriorityQueueCollator<>(subScanners);
		return new SingleUseScannerIterable<>(collator);
	}

	@Override
	default Iterable<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		List<SortedScanner<D>> subScanners = new ArrayList<>();
		for(N node : getPhysicalNodesForRanges(ranges)){
			// the scanners are wrapped in a SortedScannerIterable, so we need to unwrap them for the collator
			Scanner<D> scanner = new IteratorScanner<>(node.scanMulti(ranges, config).iterator());
			Filter<D> filter = getPartitions().getDatabeanFilterForNode(node);
			FilteringSortedScanner<D> filteredScanner = new FilteringSortedScanner<>(scanner, filter);
			subScanners.add(filteredScanner);
		}
		SortedScanner<D> collator = new PriorityQueueCollator<>(subScanners);
		return new SingleUseScannerIterable<>(collator);
	}

	/************************* helpers ******************************/

	static <T> List<T> getLimitedCopyOfResultIfNecessary(Config config, List<T> result){
		if(config == null){
			return result;
		}
		if(config.getLimit() == null){
			return result;
		}
		return DrListTool.copyOfRange(result, 0, config.getLimit());
	}
}
