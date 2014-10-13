package com.hotpads.datarouter.storage.lazy;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.concurrent.Lazy;

public class LazyIndexedStorageReaderImpl<PK extends PrimaryKey<PK>, D extends Databean<PK,D>>
implements LazyIndexedStorageReader<PK, D>{

	private IndexedStorageReader<PK, D> storage;
	
	public LazyIndexedStorageReaderImpl(IndexedStorageReader<PK,D> storage){
		this.storage = storage;
	}
	
	@Override
	public Lazy<Long> count(final Lookup<PK> lookup, final Config config){
		return new Lazy<Long>(){

			@Override
			protected Long load(){
				return storage.count(lookup, config);
			}
			
		};
	}
	
	@Override
	public Lazy<D> lookupUnique(final UniqueKey<PK> uniqueKey, final Config config){
		return new Lazy<D>(){
			
			@Override
			protected D load(){
				return storage.lookupUnique(uniqueKey, config);
			}
		};
	}
	
	@Override
	public Lazy<List<D>> lookupMultiUnique(final Collection<UniqueKey<PK>> uniqueKeys, final Config config){
		return new Lazy<List<D>>(){

			@Override
			protected List<D> load(){
				return storage.lookupMultiUnique(uniqueKeys, config);
			}
			
		};
	}
	
	@Override
	public Lazy<List<D>> lookup(final Lookup<PK> lookup, final boolean wildcardLastField, final Config config){
		return new Lazy<List<D>>(){

			@Override
			protected List<D> load(){
				return storage.lookup(lookup, wildcardLastField, config);
			}
		};
	}
	
	@Override
	public Lazy<List<D>> lookupMulti(final Collection<Lookup<PK>> lookup, final Config config){
		return new Lazy<List<D>>(){

			@Override
			protected List<D> load(){
				return storage.lookup(lookup, config);
			}
		};
	}

}
