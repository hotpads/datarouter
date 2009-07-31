package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;

public interface MapStorageReadOps<D extends Databean>{

	boolean exists(Key<D> key, Config config);
	
	D get(Key<D> key, Config config);
	List<D> getMulti(Collection<? extends Key<D>> keys, Config config);
	List<D> getAll(Config config);

//	List<K> getKeys(Collection<K> keys, Config config);
//	List<K> getAllKeys(Config config);
	
}
