package com.hotpads.datarouter.op;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;

public interface MapStorageWriteOps <D extends Databean> {

	void put(D databean, Config config);
	void putMulti(Collection<D> databeans, Config config);
	
	void delete(Key<D> key, Config config);
	void deleteMulti(Collection<? extends Key<D>> keys, Config config);
	void deleteAll(Config config);
}
