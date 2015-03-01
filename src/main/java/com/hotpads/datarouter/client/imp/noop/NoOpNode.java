package com.hotpads.datarouter.client.imp.noop;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class NoOpNode<PK extends PrimaryKey<PK>, D extends Databean<PK, D>> implements IndexedSortedMapStorage<PK, D>{

	@Override
	public boolean exists(PK key, Config config){
		return false;
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		return Collections.emptyList();
	}

	@Override
	public D get(PK key, Config config){
		return null;
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		return Collections.emptyList();
	}

	@Override
	public void put(D databean, Config config){
		
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		
	}

	@Override
	public void delete(PK key, Config config){
		
	}
	
	@Override
	public void delete(Lookup<PK> lookup, Config config){
		
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		
	}

	@Override
	public void deleteAll(Config config){
		
	}

	@Override
	public PK getFirstKey(Config config){
		return null;
	}

	@Override
	public D getFirst(Config config){
		return null;
	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		return Collections.emptyList();
	}

	@Override
	public List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config){
		return Collections.emptyList();
	}

	@Override
	public List<PK> getKeysInRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config){
		return Collections.emptyList();
	}

	@Override
	public SortedScannerIterable<PK> scanKeys(Range<PK> range, Config config){
		return new EmptySortedScannerIterable<>();
	}

	@Override
	public SortedScannerIterable<D> scan(Range<PK> range, Config config){
		return new EmptySortedScannerIterable<>();
	}

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		
	}

	@Override
	public Long count(Lookup<PK> lookup, Config config){
		return 0L;
	}

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		return null;
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		return Collections.emptyList();
	}

	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config){
		return Collections.emptyList();
	}

	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookup, Config config){
		return Collections.emptyList();
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		
	}
}
