package com.hotpads.datarouter.op;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


public interface IndexedStorageWriteOps<PK extends PrimaryKey<PK>,D extends Databean<PK>>{

	void delete(Lookup<PK> lookup, Config config);
	
	void deleteUnique(UniqueKey<PK> uniqueKey, Config config);
	void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config);
}
