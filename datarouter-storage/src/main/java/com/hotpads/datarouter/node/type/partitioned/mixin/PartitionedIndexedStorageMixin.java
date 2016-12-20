package com.hotpads.datarouter.node.type.partitioned.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.common.collect.Iterators;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.IndexedStorage;
import com.hotpads.datarouter.node.op.raw.IndexedStorage.PhysicalIndexedStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.node.type.partitioned.PartitionedNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.util.core.collections.Range;

public interface PartitionedIndexedStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalIndexedStorageNode<PK,D>>
extends IndexedStorage<PK,D>, PartitionedNode<PK,D,N>{

	@Override
	default void delete(Lookup<PK> lookup, Config config){
		for(N node : DrIterableTool.nullSafe(getPhysicalNodes())){
			node.delete(lookup, config);
		}
	}

	@Override
	default void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		for(N node : DrIterableTool.nullSafe(getPhysicalNodes())){
			node.deleteMultiUnique(uniqueKeys, config);
		}
	}

	@Override
	default void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		for(N node : DrIterableTool.nullSafe(getPhysicalNodes())){
			node.deleteUnique(uniqueKey, config);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		for(N node : DrIterableTool.nullSafe(getPhysicalNodes())){
			node.deleteByIndex(keys, config);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		for(N node : DrIterableTool.nullSafe(getPhysicalNodes())){
			node.registerManaged(managedNode);
		}
		return managedNode;
	}

	@Override
	default List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		for(N node : DrIterableTool.nullSafe(getPhysicalNodes())){
			return node.getManagedNodes();
		}
		return new ArrayList<>();
	}

	@Override
	default D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		if(uniqueKey == null){
			return null;
		}
		Collection<N> nodes = getPhysicalNodesForSecondaryKey(uniqueKey);
		//TODO randomize node access to avoid drowning first node
		for(N node : DrIterableTool.nullSafe(nodes)){
			D databean = node.lookupUnique(uniqueKey, config);
			if(databean != null){
				return databean;
			}
		}
		return null;
	}


	@Override
	default List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		if(DrCollectionTool.isEmpty(uniqueKeys)){
			return null;
		}
		Collection<N> nodes = getPhysicalNodesForSecondaryKeys(uniqueKeys);
		SortedSet<D> sortedDedupedResults = new TreeSet<>();
		//TODO randomize node access to avoid drowning first node
		for(N node : DrIterableTool.nullSafe(nodes)){
			List<D> singleNodeResults = node.lookupMultiUnique(uniqueKeys, config);
			sortedDedupedResults.addAll(DrCollectionTool.nullSafe(singleNodeResults));
		}
		return new ArrayList<>(sortedDedupedResults);
	}


	@Override
	default List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config){
		if(lookup == null){
			return null;
		}
		Collection<N> nodes = getPhysicalNodesForSecondaryKey(lookup);
		SortedSet<D> sortedDedupedResults = new TreeSet<>();
		//TODO randomize node access to avoid drowning first node
		for(N node : DrIterableTool.nullSafe(nodes)){
			List<D> singleNodeResults = node.lookup(lookup, wildcardLastField, config);
			sortedDedupedResults.addAll(DrCollectionTool.nullSafe(singleNodeResults));
		}
		return new ArrayList<>(sortedDedupedResults);
	}


	@Override
	default List<D> lookupMulti(Collection<? extends Lookup<PK>> lookups, Config config){
		if(DrCollectionTool.isEmpty(lookups)){
			return null;
		}
		Collection<N> nodes = getPhysicalNodesForSecondaryKeys(lookups);
		SortedSet<D> sortedDedupedResults = new TreeSet<>();
		//TODO randomize node access to avoid drowning first node
		for(N node : DrIterableTool.nullSafe(nodes)){
			for(Lookup<PK> lookup : lookups){
				List<D> singleNodeResults = node.lookup(lookup, false, config);
				sortedDedupedResults.addAll(DrCollectionTool.nullSafe(singleNodeResults));
			}
		}
		return new ArrayList<>(sortedDedupedResults);
	}


	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		return getPhysicalNodesForSecondaryKeys(keys).stream()
				.flatMap(physicalNode -> physicalNode.getMultiFromIndex(keys, config, indexEntryFieldInfo).stream())
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	default <IK extends PrimaryKey<IK>, IE extends IndexEntry<IK, IE, PK, D>> List<D> getMultiByIndex(
			Collection<IK> keys, Config config){
		return getPhysicalNodesForSecondaryKeys(keys).stream()
				.flatMap(physicalNode -> physicalNode.getMultiByIndex(keys, config).stream())
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IE> scanIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		List<Iterator<IE>> iterators = getPhysicalNodes().stream()
				.map(node -> node.scanIndex(indexEntryFieldInfo, range, config).iterator())
				.collect(Collectors.toList());
		return DrIterableTool.dedupeSortedIterator(Iterators.mergeSorted(iterators, Comparator.naturalOrder()));
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IK> scanIndexKeys(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		List<Iterator<IK>> iterators = getPhysicalNodes().stream()
				.map(node -> node.scanIndexKeys(indexEntryFieldInfo, range, config).iterator())
				.collect(Collectors.toList());
		return DrIterableTool.dedupeSortedIterator(Iterators.mergeSorted(iterators, Comparator.naturalOrder()));
	}

}
