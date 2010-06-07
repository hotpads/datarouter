package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface MapStorageReadOps<PK extends PrimaryKey<PK>,D extends Databean<PK>>{

	boolean exists(PK key, Config config);
	
	D get(PK key, Config config);
	List<D> getMulti(Collection<PK> keys, Config config);
	List<D> getAll(Config config);
	
	List<PK> getKeys(final Collection<PK> keys, final Config config);
//	List<K> getAllKeys(Config config);
}
