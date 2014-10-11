package com.hotpads.datarouter.storage.lazy;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.IndexedMapStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.concurrent.Lazy;

public class LazyIndexedMapStorageReader<PK extends PrimaryKey<PK>, D extends Databean<PK,D>>
extends LazyMapStorageReader<PK, D>{
	
	private LazyIndexedStorageReader<PK, D> storage;
	
	public LazyIndexedMapStorageReader(IndexedMapStorageReader<PK,D> storage){
		super(storage);
		this.storage = new LazyIndexedStorageReader<PK, D>(storage);
	}
	
	public Lazy<List<D>> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config){
		return storage.lookup(lookup, wildcardLastField, config);
	}
	
	public Lazy<List<D>> lookupMulti(Collection<Lookup<PK>> lookup, Config config){
		return storage.lookupMulti(lookup, config);
	}

}
