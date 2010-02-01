package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.lookup.Lookup;


public interface IndexedStorageReadOps<D extends Databean> {

	List<D> lookup(Lookup<D> lookup, Config config);
	List<D> lookup(Collection<? extends Lookup<D>> lookup, Config config);
	
}
