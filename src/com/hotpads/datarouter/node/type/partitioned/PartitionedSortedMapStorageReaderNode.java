package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import com.google.common.collect.Multimap;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.PhysicalSortedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.collate.Collator;
import com.hotpads.util.core.iterable.scanner.collate.PriorityQueueCollator;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

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

	@Override
	public List<D> getPrefixedRange(PK prefix, boolean wildcardLastField, final PK start, final boolean startInclusive,
			Config config){
		List<D> all = ListTool.createArrayList();
		Multimap<N,PK> prefixesByPhysicalNode = getPrefixesByPhysicalNode(ListTool.wrap(prefix), wildcardLastField);
		for(N node : prefixesByPhysicalNode.keySet()){
			all.addAll(node.getPrefixedRange(prefix, wildcardLastField, start, startInclusive, config));
		}
		if(CollectionTool.isEmpty(all)){ return all; }
		Collections.sort(all);
		if(config!=null && config.getLimit()!=null && config.getLimit() < all.size()){
			List<D> limited = ListTool.copyOfRange(all, 0, config.getLimit());
			return limited;
		}else{
			return all;
		}
	}

	@Override
	public List<PK> getKeysInRange(final PK start, final boolean startInclusive, final PK end,
			final boolean endInclusive, final Config config){
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		Collection<N> physicalNodes = CollectionTool.nullSafe(getPhysicalNodesForRange(range));
		SortedSet<PK> sortedDedupedResults = SetTool.createTreeSet();
		for(N node : IterableTool.nullSafe(physicalNodes)){
			List<PK> resultFromSingleNode = node.getKeysInRange(start, startInclusive, end, endInclusive, config);
			sortedDedupedResults.addAll(CollectionTool.nullSafe(resultFromSingleNode));
		}
		List<PK> resultList = ListTool.createArrayList(sortedDedupedResults);
		return getLimitedCopyOfResultIfNecessary(config, resultList);
	}

	@Override
	public List<D> getRange(final PK start, final boolean startInclusive, final PK end, final boolean endInclusive,
			final Config config){
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		Collection<N> physicalNodes = CollectionTool.nullSafe(getPhysicalNodesForRange(range));
		SortedSet<D> sortedDedupedResults = SetTool.createTreeSet();
		for(N node : IterableTool.nullSafe(physicalNodes)){
			List<D> resultFromSingleNode = node.getRange(start, startInclusive, end, endInclusive, config);
			sortedDedupedResults.addAll(CollectionTool.nullSafe(resultFromSingleNode));
		}
		List<D> resultList = ListTool.createArrayList(sortedDedupedResults);
		return getLimitedCopyOfResultIfNecessary(config, resultList);
	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		return getWithPrefixes(ListTool.wrap(prefix), wildcardLastField, config);
	}

	@Override
	public List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config) {
		List<D> all = ListTool.createArrayList();
		Multimap<N,PK>	prefixesByNode = getPrefixesByPhysicalNode(prefixes, wildcardLastField);
		for(N node : prefixesByNode.keySet()){
			Collection<PK> prefixesForNode = prefixesByNode.get(node);
			all.addAll(node.getWithPrefixes(prefixesForNode, wildcardLastField, config));
		}
		if(CollectionTool.isEmpty(all)){ return all; }
		Collections.sort(all);
		if(config!=null && config.getLimit()!=null && config.getLimit() < all.size()){
			List<D> limited = ListTool.copyOfRange(all, 0, config.getLimit());
			return limited;
		}else{
			return all;
		}
	}
	
	@Override
	public SortedScannerIterable<PK> scanKeys(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config){
		List<SortedScanner<PK>> subScanners = ListTool.createArrayList();
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		List<N> nodes = getPhysicalNodesForRange(range);
		for(N node : IterableTool.nullSafe(CollectionTool.nullSafe(nodes))){
			//the scanners are wrapped in a SortedScannerIterable, so we need to unwrap them for the collator
			SortedScannerIterable<PK> iterable = node.scanKeys(start, startInclusive, end, endInclusive, config);
//			FilteredSortedScanner<PK> filteredScanner = new FilteredSortedScannerImp<PK>(iterable.getScanner(), filter);
			subScanners.add(iterable.getScanner());
		}
		Collator<PK> collator = new PriorityQueueCollator<PK>(subScanners);
		return new SortedScannerIterable<PK>(collator);
	}
	
	@Override
	public SortedScannerIterable<D> scan(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config){
		List<SortedScanner<D>> subScanners = ListTool.createArrayList();
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		List<N> nodes = getPhysicalNodesForRange(range);
		for(N node : IterableTool.nullSafe(nodes)){
			//the scanners are wrapped in a SortedScannerIterable, so we need to unwrap them for the collator
			SortedScannerIterable<D> iterable = node.scan(start, startInclusive, end, endInclusive, config);
			subScanners.add(iterable.getScanner());
		}
		Collator<D> collator = new PriorityQueueCollator<D>(subScanners);
		return new SortedScannerIterable<D>(collator);
	}
	
	
	/************************* helpers ******************************/
	
	protected <T> List<T> getLimitedCopyOfResultIfNecessary(Config config, List<T> result){
		if(config==null){ return result; }
		if(config.getLimit()==null){ return result; }
		return ListTool.copyOfRange(result, 0, config.getLimit());
	}
	
}
