package com.hotpads.datarouter.op;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


public interface IndexedStorageWriteOps<D extends Databean<PK>,PK extends PrimaryKey<PK>>{

	void delete(Lookup<PK> lookup, Config config);

}
