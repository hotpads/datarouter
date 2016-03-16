package com.hotpads.datarouter.storage.lazy.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.lazy.base.LazyReader;
import com.hotpads.util.core.concurrent.Lazy;

public interface LazySortedStorageReaderMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		S extends SortedMapStorageReader<PK,D>>
extends LazyReader<PK,D,S>{

	@Deprecated
	public default Lazy<List<D>> getWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config){
		return Lazy.of(() -> getBackingStorage().getWithPrefix(prefix, wildcardLastField, config));
	}

	@Deprecated
	public default Lazy<List<D>> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField,
			final Config config){
		return Lazy.of(() -> getBackingStorage().getWithPrefixes(prefixes, wildcardLastField, config));
	}

}
