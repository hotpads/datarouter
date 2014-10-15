package com.hotpads.datarouter.storage.lazy;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.IndexedSortedMapStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.concurrent.Lazy;

public class LazyIndexedSortedMapStorageReader<PK extends PrimaryKey<PK>, D extends Databean<PK, D>>
extends LazyIndexedMapStorageReader<PK, D>
implements LazySortedStorageReader<PK, D>{

	private LazySortedStorageReaderImpl<PK, D> storage;

	public LazyIndexedSortedMapStorageReader(IndexedSortedMapStorageReader<PK, D> storage){
		super(storage);
		this.storage = new LazySortedStorageReaderImpl<>(storage);
	}

	@Override
	public Lazy<PK> getFirstKey(Config config){
		return storage.getFirstKey(config);
	}

	@Override
	public Lazy<D> getFirst(Config config){
		return storage.getFirst(config);
	}

	@Override
	public Lazy<List<D>> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		return storage.getWithPrefix(prefix, wildcardLastField, config);
	}

	@Override
	public Lazy<List<D>> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config){
		return storage.getWithPrefixes(prefixes, wildcardLastField, config);
	}

}
