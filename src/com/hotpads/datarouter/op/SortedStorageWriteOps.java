package com.hotpads.datarouter.op;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;



public interface SortedStorageWriteOps<D extends Databean,PK extends PrimaryKey<D>> 
extends MapStorageWriteOps<D,PK>{

	void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config);

//	void deleteRange(Key<D> start, boolean startInclusive, Key<D> end, boolean endInclusive, Config config);
}
