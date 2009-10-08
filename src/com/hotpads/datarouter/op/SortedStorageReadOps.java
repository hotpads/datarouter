package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;


public interface SortedStorageReadOps<D extends Databean> 
extends MapStorageReadOps<D>{

//	Key<D> getFirstKey(Config config);
	D getFirst(Config config);
	
//	Key<D> getLastKey(Config config);
//	D getLast(Config config);
//	
//	List<Key<D>> getKeysWithPrefix(Key<D> prefix, Config config);
	List<D> getWithPrefix(Key<D> prefix, boolean wildcardLastField, Config config);
	List<D> getWithPrefixes(Collection<? extends Key<D>> prefixes, boolean wildcardLastField, Config config);
//
//	List<Key<D>> getKeysInRange(Key<D> start, boolean startInclusive, Key<D> end, boolean endInclusive, Config config);
	List<D> getRange(Key<D> start, boolean startInclusive, Key<D> end, boolean endInclusive, Config config);
//	
//	Iterator<Key<D>> scanKeys(Key<D> start, boolean startInclusive, Config config);
//	Iterator<D> scan(Key<D> start, boolean startInclusive, Config config);
	
}
