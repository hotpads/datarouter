package com.hotpads.datarouter.storage.lazy;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.concurrent.Lazy;

public interface LazySortedStorageReader<PK extends PrimaryKey<PK>, D extends Databean<PK, D>>{

	Lazy<PK> getFirstKey(Config config);
	
	Lazy<D> getFirst(Config config);
	
	Lazy<List<D>> getWithPrefix(PK prefix, boolean wildcardLastField, Config config);
	
	Lazy<List<D>> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config);
	
}
