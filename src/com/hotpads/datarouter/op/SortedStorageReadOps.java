package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


public interface SortedStorageReadOps<D extends Databean<PK>,PK extends PrimaryKey<PK>>
extends MapStorageReadOps<D,PK>{

	PK getFirstKey(Config config);
	D getFirst(Config config);
//	
//	List<Key<D>> getKeysWithPrefix(Key<D> prefix, Config config);
	List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config);
	List<D> getWithPrefixes(Collection<? extends PK> prefixes, boolean wildcardLastField, Config config);

	List<PK> getKeysInRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config);
	List<D> getRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config);

	List<D> getPrefixedRange(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config config);
	
//	Iterator<Key<D>> scanKeys(Key<D> start, boolean startInclusive, Config config);
//	Iterator<D> scan(Key<D> start, boolean startInclusive, Config config);
	
}
