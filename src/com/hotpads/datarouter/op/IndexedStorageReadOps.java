package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;


public interface IndexedStorageReadOps<D extends Databean,PK extends PrimaryKey<D>> {

	List<D> lookup(Lookup<D> lookup, Config config);
	List<D> lookup(Collection<? extends Lookup<D>> lookup, Config config);
	
}
