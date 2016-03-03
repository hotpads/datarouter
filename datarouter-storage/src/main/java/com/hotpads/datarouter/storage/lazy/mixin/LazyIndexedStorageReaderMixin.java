package com.hotpads.datarouter.storage.lazy.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.lazy.base.LazyReader;
import com.hotpads.util.core.concurrent.Lazy;

public interface LazyIndexedStorageReaderMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		S extends IndexedStorageReader<PK,D>>
extends LazyReader<PK,D,S>{

	public default Lazy<D> lookupUnique(final UniqueKey<PK> uniqueKey, final Config config){
		return Lazy.of(() -> { return getBackingStorage().lookupUnique(uniqueKey, config); });
	}

	public default Lazy<List<D>> lookupMultiUnique(final Collection<? extends UniqueKey<PK>> uniqueKeys,
			final Config config){
		return Lazy.of(() -> { return getBackingStorage().lookupMultiUnique(uniqueKeys, config); });
	}

	public default Lazy<List<D>> lookup(final Lookup<PK> lookup, final boolean wildcardLastField, final Config config){
		return Lazy.of(() -> { return getBackingStorage().lookup(lookup, wildcardLastField, config); });
	}

	public default Lazy<List<D>> lookupMulti(final Collection<? extends Lookup<PK>> lookup, final Config config){
		return Lazy.of(() -> { return getBackingStorage().lookupMulti(lookup, config); });
	}

}
