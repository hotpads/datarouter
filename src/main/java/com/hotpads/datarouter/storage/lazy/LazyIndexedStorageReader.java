package com.hotpads.datarouter.storage.lazy;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class LazyIndexedStorageReader<PK extends PrimaryKey<PK>, D extends Databean<PK,D>>{

	private IndexedStorageReader<PK, D> storage;
	
	public LazyIndexedStorageReader(IndexedStorageReader<PK,D> storage){
		this.storage = storage;
	}
	
	public Lazy<List<D>> lookup(final Lookup<PK> lookup, final boolean wildcardLastField, final Config config){
		return new Lazy<List<D>>(){

			@Override
			protected List<D> load(){
				return storage.lookup(lookup, wildcardLastField, config);
			}
		};
	}
	
	public Lazy<List<D>> lookupMulti(final Collection<Lookup<PK>> lookup, final Config config){
		return new Lazy<List<D>>(){

			@Override
			protected List<D> load(){
				return storage.lookup(lookup, config);
			}
		};
	}
}
