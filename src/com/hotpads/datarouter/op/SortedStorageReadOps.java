package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.iterable.PeekableIterable;


public interface SortedStorageReadOps<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends MapStorageReadOps<PK,D>{

	PK getFirstKey(Config config);
	D getFirst(Config config);
//	
//	List<Key<D>> getKeysWithPrefix(Key<D> prefix, Config config);
	List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config);
	List<D> getWithPrefixes(Collection<? extends PK> prefixes, boolean wildcardLastField, Config config);

	List<PK> getKeysInRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config);
	List<D> getRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config);

	List<D> getPrefixedRange(
			final PK prefix, final boolean wildcardLastField, 
			final PK start, final boolean startInclusive, 
			final Config config);
	
//	Iterator<Key<D>> scanKeys(Key<D> start, boolean startInclusive, Config config);
	PeekableIterable<D> scan(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config config);
}
