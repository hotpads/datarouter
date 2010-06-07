package com.hotpads.datarouter.op;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface MapStorageWriteOps<PK extends PrimaryKey<PK>,D extends Databean<PK>>{

	void put(D databean, Config config);
	void putMulti(Collection<D> databeans, Config config);
	
	void delete(PK key, Config config);
	void deleteMulti(Collection<PK> keys, Config config);
	void deleteAll(Config config);
}
