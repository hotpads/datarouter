package com.hotpads.datarouter.op;

import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;


public interface SortedStorageReadOps<D extends Databean> 
extends MapStorageReadOps<D>{

	Key<D> getFirstKey();
	D getFirst();
	
	Key<D> getLastKey();
	D getLast();
	
	List<Key<D>> getPrefixedKeys(Key<D> prefix);
	List<D> getPrefixedList(Key<D> prefix);

	List<Key<D>> getKeyRange(Key<D> start, boolean startInclusive, Key<D> end, boolean endInclusive);
	List<D> getRange(Key<D> start, boolean startInclusive, Key<D> end, boolean endInclusive);
	
	List<Key<D>> getKeyRangeFrom(Key<D> start, boolean startInclusive);
	List<D> getRangeFrom(Key<D> start, boolean startInclusive);
	
}
