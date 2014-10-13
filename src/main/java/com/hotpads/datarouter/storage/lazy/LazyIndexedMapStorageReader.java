package com.hotpads.datarouter.storage.lazy;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.IndexedMapStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.concurrent.Lazy;

public class LazyIndexedMapStorageReader<PK extends PrimaryKey<PK>, D extends Databean<PK,D>>
extends LazyMapStorageReader<PK, D> implements LazyIndexedStorageReader<PK, D>{
	
	private LazyIndexedStorageReaderImpl<PK, D> storage;
	
	public LazyIndexedMapStorageReader(IndexedMapStorageReader<PK,D> storage){
		super(storage);
		this.storage = new LazyIndexedStorageReaderImpl<PK, D>(storage);
	}
	
	@Override
	public Lazy<Long> count(final Lookup<PK> lookup, final Config config){
		return storage.count(lookup, config);
	}
	
	@Override
	public Lazy<D> lookupUnique(final UniqueKey<PK> uniqueKey, final Config config){
		return storage.lookupUnique(uniqueKey, config);
	}
	
	@Override
	public Lazy<List<D>> lookupMultiUnique(final Collection<UniqueKey<PK>> uniqueKeys, final Config config){
		return storage.lookupMultiUnique(uniqueKeys, config);
	}
	
	@Override
	public Lazy<List<D>> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config){
		return storage.lookup(lookup, wildcardLastField, config);
	}
	
	@Override
	public Lazy<List<D>> lookupMulti(Collection<Lookup<PK>> lookup, Config config){
		return storage.lookupMulti(lookup, config);
	}

}
