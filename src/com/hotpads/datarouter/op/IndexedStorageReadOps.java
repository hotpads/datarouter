package com.hotpads.datarouter.op;

import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.index.Lookup;

public interface IndexedStorageReadOps<D extends Databean> {

	List<D> lookup(Lookup<D> lookup, Config config);
	
}
