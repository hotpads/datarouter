package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import com.google.common.collect.Multimap;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.PhysicalSortedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrSetTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.collate.Collator;
import com.hotpads.util.core.iterable.scanner.collate.PriorityQueueCollator;
import com.hotpads.util.core.iterable.scanner.filter.Filter;
import com.hotpads.util.core.iterable.scanner.filter.FilteringSortedScanner;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

public abstract class PartitionedSortedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalSortedMapStorageReaderNode<PK,D>>
extends PartitionedMapStorageReaderNode<PK,D,F,N>
implements SortedMapStorageReaderNode<PK,D>{
	
	public PartitionedSortedMapStorageReaderNode(Class<D> databeanClass, Class<F> fielderClass, Datarouter router) {
		super(databeanClass, fielderClass, router);
	}

	/************************* sorted storage methods *****************************/
	
	@Override
	public D getFirst(Config config){
		SortedSet<D> firstFromEachNode = DrSetTool.createTreeSet();
		Collection<N> physicalNodes = getPhysicalNodesForFirst();
		for(N node : DrIterableTool.nullSafe(physicalNodes)){
			D databean = node.getFirst(config);
			if(databean==null){ continue; }
			firstFromEachNode.add(databean);
		}
		return DrCollectionTool.getFirst(firstFromEachNode);
	}

	@Override
	public PK getFirstKey(Config config){
		SortedSet<PK> firstFromEachNode = DrSetTool.createTreeSet();
		Collection<N> physicalNodes = getPhysicalNodesForFirst();
		for(N node : DrIterableTool.nullSafe(physicalNodes)){
			PK key = node.getFirstKey(config);
			if(key==null){ continue; }
			firstFromEachNode.add(key);
		}
		return DrCollectionTool.getFirst(firstFromEachNode);
	}

	@Override
	public List<D> getPrefixedRange(PK prefix, boolean wildcardLastField, final PK start, final boolean startInclusive,
			Config config){
		SortedSet<D> sortedDedupedResults = DrSetTool.createTreeSet();
		Multimap<N,PK> prefixesByPhysicalNode = getPrefixesByPhysicalNode(DrListTool.wrap(prefix), wildcardLastField);
		for(N node : prefixesByPhysicalNode.keySet()){
			List<D> allFromNode = node.getPrefixedRange(prefix, wildcardLastField, start, startInclusive, config);
			List<D> filtered = filterDatabeansForPhysicalNode(allFromNode, node);
			sortedDedupedResults.addAll(filtered);
		}
		List<D> resultList = DrListTool.createArrayList(sortedDedupedResults);
		return getLimitedCopyOfResultIfNecessary(config, resultList);
	}

	@Override
	public List<PK> getKeysInRange(final PK start, final boolean startInclusive, final PK end,
			final boolean endInclusive, final Config config){
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		SortedSet<PK> sortedDedupedResults = DrSetTool.createTreeSet();
		Collection<N> physicalNodes = getPhysicalNodesForRange(range);
		for(N node : DrIterableTool.nullSafe(physicalNodes)){
			List<PK> resultFromSingleNode = node.getKeysInRange(start, startInclusive, end, endInclusive, config);
			sortedDedupedResults.addAll(DrCollectionTool.nullSafe(resultFromSingleNode));
		}
		List<PK> resultList = DrListTool.createArrayList(sortedDedupedResults);
		return getLimitedCopyOfResultIfNecessary(config, resultList);
	}

	@Override
	public List<D> getRange(final PK start, final boolean startInclusive, final PK end, final boolean endInclusive,
			final Config config){
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		Collection<N> physicalNodes = DrCollectionTool.nullSafe(getPhysicalNodesForRange(range));
		SortedSet<D> sortedDedupedResults = DrSetTool.createTreeSet();
		for(N node : DrIterableTool.nullSafe(physicalNodes)){
			List<D> resultFromSingleNode = node.getRange(start, startInclusive, end, endInclusive, config);
			sortedDedupedResults.addAll(DrCollectionTool.nullSafe(resultFromSingleNode));
		}
		List<D> resultList = DrListTool.createArrayList(sortedDedupedResults);
		return getLimitedCopyOfResultIfNecessary(config, resultList);
	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		return getWithPrefixes(DrListTool.wrap(prefix), wildcardLastField, config);
	}

	@Override
	public List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config) {
		List<D> all = DrListTool.createArrayList();
		Multimap<N,PK>	prefixesByNode = getPrefixesByPhysicalNode(prefixes, wildcardLastField);
		for(N node : prefixesByNode.keySet()){
			Collection<PK> prefixesForNode = prefixesByNode.get(node);
			all.addAll(node.getWithPrefixes(prefixesForNode, wildcardLastField, config));
		}
		if(DrCollectionTool.isEmpty(all)){ return all; }
		Collections.sort(all);
		return getLimitedCopyOfResultIfNecessary(config, all);
	}
	
	//TODO add option to the BasePartitionedNode to skip filtering when not needed
	@Override
	public SortedScannerIterable<PK> scanKeys(Range<PK> pRange, Config config){
		Range<PK> range = Range.nullSafe(pRange);
		List<SortedScanner<PK>> subScanners = DrListTool.createArrayList();
		List<N> nodes = getPhysicalNodesForRange(range);
		for(N node : DrIterableTool.nullSafe(nodes)){
			SortedScannerIterable<PK> iterable = node.scanKeys(range, config);
			Filter<PK> filter = partitions.getPrimaryKeyFilterForNode(node);
			FilteringSortedScanner<PK> filteredScanner = new FilteringSortedScanner<PK>(iterable.getScanner(), filter);
			subScanners.add(filteredScanner);
		}
		Collator<PK> collator = new PriorityQueueCollator<PK>(subScanners);
		return new SortedScannerIterable<PK>(collator);
	}
	
	@Override
	public SortedScannerIterable<D> scan(Range<PK> pRange, Config config){
		Range<PK> range = Range.nullSafe(pRange);
		List<SortedScanner<D>> subScanners = DrListTool.createArrayList();
		List<N> nodes = getPhysicalNodesForRange(range);
		for(N node : DrIterableTool.nullSafe(nodes)){
			//the scanners are wrapped in a SortedScannerIterable, so we need to unwrap them for the collator
			SortedScannerIterable<D> iterable = node.scan(range, config);
			Filter<D> filter = partitions.getDatabeanFilterForNode(node);
			FilteringSortedScanner<D> filteredScanner = new FilteringSortedScanner<D>(iterable.getScanner(), filter);
			subScanners.add(filteredScanner);
		}
		Collator<D> collator = new PriorityQueueCollator<D>(subScanners);
		return new SortedScannerIterable<D>(collator);
	}
	
	
	/************************* helpers ******************************/
	
	protected <T> List<T> getLimitedCopyOfResultIfNecessary(Config config, List<T> result){
		if(config==null){ return result; }
		if(config.getLimit()==null){ return result; }
		return DrListTool.copyOfRange(result, 0, config.getLimit());
	}
	
}
