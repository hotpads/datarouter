package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public interface MapStorageReadOps<D extends Databean>{

	boolean exists(UniqueKey<D> key, Config config);
	

	//TODO lookup should not extend key because you could pass one in here... or fix that problem somehow
	D get(UniqueKey<D> key, Config config);
	List<D> getMulti(Collection<? extends UniqueKey<D>> keys, Config config);
	List<D> getAll(Config config);

//	List<K> getKeys(Collection<K> keys, Config config);
//	List<K> getAllKeys(Config config);
	
}
