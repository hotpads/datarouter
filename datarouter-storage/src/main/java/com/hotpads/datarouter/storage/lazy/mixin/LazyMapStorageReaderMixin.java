package com.hotpads.datarouter.storage.lazy.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.lazy.base.LazyReader;
import com.hotpads.util.core.concurrent.Lazy;

public interface LazyMapStorageReaderMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		S extends MapStorageReader<PK,D>>
extends LazyReader<PK,D,S>{

	public default Lazy<D> get(final PK key, final Config config){
		return Lazy.of(() -> getBackingStorage().get(key, config) );
	}

	public default Lazy<List<D>> getMulti(final Collection<PK> keys, final Config config){
		return Lazy.of(() -> getBackingStorage().getMulti(keys, config));
	}

	public default Lazy<List<PK>> getKeys(final Collection<PK> keys, final Config config){
		return Lazy.of(() -> getBackingStorage().getKeys(keys, config));
	}

}
