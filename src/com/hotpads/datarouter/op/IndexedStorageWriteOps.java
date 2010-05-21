package com.hotpads.datarouter.op;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;


public interface IndexedStorageWriteOps <D extends Databean> {

	void delete(Lookup<D> lookup, Config config);

}
