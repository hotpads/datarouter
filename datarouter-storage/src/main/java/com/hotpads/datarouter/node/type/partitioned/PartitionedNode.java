package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public interface PartitionedNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalNode<PK,D>>{

	List<N> getPhysicalNodes();
	N getPhysicalNode(PK key);
	Partitions<PK,D,N> getPartitions();

	ArrayListMultimap<N,PK> getPrimaryKeysByPhysicalNode(Collection<PK> pks);
	ArrayListMultimap<N,D> getDatabeansByPhysicalNode(Collection<D> databeans);

	<IK extends Key<?>> List<N> getPhysicalNodesForSecondaryKey(IK key);
	<IK extends Key<?>> List<N> getPhysicalNodesForSecondaryKeys(Collection<IK> keys);

	List<N> getPhysicalNodesForRange(Range<PK> range);
	Multimap<N,PK> getPrefixesByPhysicalNode(Collection<PK> prefixes, boolean wildcardLastField);
	Set<N> getPhysicalNodesForRanges(Collection<Range<PK>> ranges);

}
