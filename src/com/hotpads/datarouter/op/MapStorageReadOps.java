package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public interface MapStorageReadOps<D extends Databean<PK>,PK extends PrimaryKey<PK>>{

	boolean exists(UniqueKey<PK> key, Config config);
	

	//TODO lookup should not extend key because you could pass one in here... or fix that problem somehow
	D get(UniqueKey<PK> key, Config config);
	List<D> getMulti(Collection<? extends UniqueKey<PK>> keys, Config config);
	List<D> getAll(Config config);

//	List<K> getKeys(Collection<K> keys, Config config);
//	List<K> getAllKeys(Config config);
	
}
