package com.hotpads.datarouter.op;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public interface MapStorageWriteOps<D extends Databean<PK>,PK extends PrimaryKey<PK>>{

	void put(D databean, Config config);
	void putMulti(Collection<D> databeans, Config config);
	
	void delete(UniqueKey<PK> key, Config config);
	void deleteMulti(Collection<? extends UniqueKey<PK>> keys, Config config);
	void deleteAll(Config config);
}
