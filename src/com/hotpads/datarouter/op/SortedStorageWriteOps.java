package com.hotpads.datarouter.op;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;



public interface SortedStorageWriteOps<D extends Databean> 
extends MapStorageWriteOps<D>{

	void deleteRange(Key<D> start, boolean startInclusive, Key<D> end, boolean endInclusive);
	void deleteWithPrefix(Key<D> prefix);
}
