package com.hotpads.datarouter.op;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


public interface SortedStorageWriteOps<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends MapStorageWriteOps<PK,D>{

	void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config);

//	void deleteRange(Key<D> start, boolean startInclusive, Key<D> end, boolean endInclusive, Config config);
}
