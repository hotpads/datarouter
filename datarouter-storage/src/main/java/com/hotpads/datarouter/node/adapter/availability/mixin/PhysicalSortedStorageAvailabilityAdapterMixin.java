package com.hotpads.datarouter.node.adapter.availability.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.UnavailableException;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage.PhysicalSortedStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public interface PhysicalSortedStorageAvailabilityAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalSortedStorageNode<PK,D>>
extends SortedStorage<PK,D>{

	N getBackingNode();
	UnavailableException makeUnavailableException();

	@Override
	@Deprecated
	default List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().getWithPrefix(prefix, wildcardLastField, config);
		}
		throw makeUnavailableException();
	}

	@Override
	@Deprecated
	default List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().getWithPrefixes(prefixes, wildcardLastField, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default Iterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().scanKeysMulti(ranges, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default Iterable<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().scanMulti(ranges, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		if(getBackingNode().getClient().isAvailable()){
			getBackingNode().deleteRangeWithPrefix(prefix, wildcardLastField, config);
			return;
		}
		throw makeUnavailableException();
	}

}
