package com.hotpads.datarouter.op;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public interface MapStorageWriteOps <D extends Databean,PK extends PrimaryKey<D>> {

	void put(D databean, Config config);
	void putMulti(Collection<D> databeans, Config config);
	
	void delete(UniqueKey<D> key, Config config);
	void deleteMulti(Collection<? extends UniqueKey<D>> keys, Config config);
	void deleteAll(Config config);
}
