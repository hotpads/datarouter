package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


public interface IndexedStorageReadOps<PK extends PrimaryKey<PK>,D extends Databean<PK>> {

	List<D> lookup(Lookup<PK> lookup, Config config);
	List<D> lookup(Collection<? extends Lookup<PK>> lookup, Config config);
	
}
