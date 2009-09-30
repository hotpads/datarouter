package com.hotpads.datarouter.op;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;



public interface SortedStorageWriteOps<D extends Databean> 
extends MapStorageWriteOps<D>{

	void deleteRangeWithPrefix(Key<D> prefix, boolean wildcardLastField, Config config);

//	void deleteRange(Key<D> start, boolean startInclusive, Key<D> end, boolean endInclusive, Config config);
}
