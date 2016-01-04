package com.hotpads.datarouter.node.type.partitioned;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Multimap;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.PhysicalSortedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.Scanner;
import com.hotpads.util.core.iterable.scanner.collate.PriorityQueueCollator;
import com.hotpads.util.core.iterable.scanner.filter.Filter;
import com.hotpads.util.core.iterable.scanner.filter.FilteringSortedScanner;
import com.hotpads.util.core.iterable.scanner.iterable.IteratorScanner;
import com.hotpads.util.core.iterable.scanner.iterable.SingleUseScannerIterable;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

public abstract class PartitionedSortedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalSortedMapStorageReaderNode<PK,D>>
extends PartitionedMapStorageReaderNode<PK,D,F,N>
implements SortedMapStorageReaderNode<PK,D>{

	public PartitionedSortedMapStorageReaderNode(Supplier<D> databeanSupplier, Supplier<F> fielderSupplier,
			Router router) {
		super(databeanSupplier, fielderSupplier, router);
	}

	/************************* sorted storage methods *****************************/

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		return getWithPrefixes(DrListTool.wrap(prefix), wildcardLastField, config);
	}

	@Override
	@Deprecated
	public List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config) {
		List<D> all = new ArrayList<>();
		Multimap<N,PK>	prefixesByNode = getPrefixesByPhysicalNode(prefixes, wildcardLastField);
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

	//TODO add option to the BasePartitionedNode to skip filtering when not needed
	@Override
	public SingleUseScannerIterable<PK> scanKeys(Range<PK> range, Config config){
		range = Range.nullSafe(range);
		List<SortedScanner<PK>> subScanners = new ArrayList<>();
		List<N> nodes = getPhysicalNodesForRange(range);
		for(N node : DrIterableTool.nullSafe(nodes)){
			Scanner<PK> scanner = new IteratorScanner<>(node.scanKeys(range, config).iterator());
			Filter<PK> filter = partitions.getPrimaryKeyFilterForNode(node);
			FilteringSortedScanner<PK> filteredScanner = new FilteringSortedScanner<>(scanner, filter);
			subScanners.add(filteredScanner);
		}
		SortedScanner<PK> collator = new PriorityQueueCollator<>(subScanners);
		return new SingleUseScannerIterable<>(collator);
	}

	@Override
	public SingleUseScannerIterable<D> scan(Range<PK> range, Config config){
		final Range<PK> nullSaferange = Range.nullSafe(range);
		List<SortedScanner<D>> subScanners = new ArrayList<>();
		List<N> nodes = getPhysicalNodesForRange(nullSaferange);
		for(N node : DrIterableTool.nullSafe(nodes)){
			//the scanners are wrapped in a SortedScannerIterable, so we need to unwrap them for the collator
			Scanner<D> scanner = new IteratorScanner<>(node.scan(nullSaferange, config).iterator());
			Filter<D> filter = partitions.getDatabeanFilterForNode(node);
			FilteringSortedScanner<D> filteredScanner = new FilteringSortedScanner<>(scanner, filter);
			subScanners.add(filteredScanner);
		}
		SortedScanner<D> collator = new PriorityQueueCollator<>(subScanners);
		return new SingleUseScannerIterable<>(collator);
	}


	/************************* helpers ******************************/

	protected <T> List<T> getLimitedCopyOfResultIfNecessary(Config config, List<T> result){
		if(config==null){
			return result;
		}
		if(config.getLimit()==null){
			return result;
		}
		return DrListTool.copyOfRange(result, 0, config.getLimit());
	}

}
