package com.hotpads.datarouter.storage.lazy;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.concurrent.Lazy;

public interface LazyIndexedStorageReader<PK extends PrimaryKey<PK>, D extends Databean<PK, D>> {

	Lazy<Long> count(Lookup<PK> lookup, Config config);

	Lazy<D> lookupUnique(UniqueKey<PK> uniqueKey, Config config);

	Lazy<List<D>> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config);

	Lazy<List<D>> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config);

	Lazy<List<D>> lookupMulti(Collection<? extends Lookup<PK>> lookup, Config config);

}