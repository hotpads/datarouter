package com.hotpads.datarouter.op;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public interface MapStorageWriteOps <D extends Databean> {

	void put(D databean, Config config);
	void putMulti(Collection<D> databeans, Config config);
	
	void delete(UniqueKey<D> key, Config config);
	void deleteMulti(Collection<? extends UniqueKey<D>> keys, Config config);
	void deleteAll(Config config);
}
