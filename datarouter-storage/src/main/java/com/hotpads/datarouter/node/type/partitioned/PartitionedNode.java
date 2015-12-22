package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PartitionedNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalNode<PK,D>>{

	List<N> getPhysicalNodes();
	N getPhysicalNode(PK key);

	ArrayListMultimap<N,PK> getPrimaryKeysByPhysicalNode(Collection<PK> pks);
	ArrayListMultimap<N,D> getDatabeansByPhysicalNode(Collection<D> databeans);

	<IK extends Key<?>> List<N> getPhysicalNodesForSecondaryKey(IK key);
	<IK extends Key<?>> List<N> getPhysicalNodesForSecondaryKeys(Collection<IK> keys);

}
