package com.hotpads.datarouter.node.type.indexing.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage.SortedStorageNode;
import com.hotpads.datarouter.node.op.raw.index.IndexListener;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public interface IndexingSortedStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedStorageNode<PK,D>>
extends SortedStorage<PK,D>{

	N getBackingNode();
	List<IndexListener<PK,D>> getIndexNodes();

	@Override
	@Deprecated
	default List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		return getBackingNode().getWithPrefix(prefix,wildcardLastField, config);
	}

	@Override
	@Deprecated
	default List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config) {
		return getBackingNode().getWithPrefixes(prefixes, wildcardLastField, config);
	}

	@Override
	default Iterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		return getBackingNode().scanKeysMulti(ranges, config);
	}

	@Override
	default Iterable<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		return getBackingNode().scanMulti(ranges, config);
	}

	@Override
	default void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		getBackingNode().deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}

}
