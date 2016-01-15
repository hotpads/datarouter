package com.hotpads.datarouter.node.type.redundant.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage.SortedStorageNode;
import com.hotpads.datarouter.node.type.redundant.RedundantNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public interface RedundantSortedStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedStorageNode<PK,D>>
extends SortedStorage<PK,D>, RedundantNode<PK,D,N>{

	@Override
	default void deleteRangeWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		for(N n : getWriteNodes()){
			n.deleteRangeWithPrefix(prefix, wildcardLastField, config);
		}
	}

	@Override
	@Deprecated
	default List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		return getReadNode().getWithPrefix(prefix,wildcardLastField, config);
	}

	@Override
	@Deprecated
	default List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config) {
		return getReadNode().getWithPrefixes(prefixes, wildcardLastField, config);
	}

	@Override
	default Iterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		return getReadNode().scanKeysMulti(ranges, config);
	}

	@Override
	default Iterable<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		return getReadNode().scanMulti(ranges, config);
	}
}
