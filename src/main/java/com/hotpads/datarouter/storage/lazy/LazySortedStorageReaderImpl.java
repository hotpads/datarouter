package com.hotpads.datarouter.storage.lazy;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.concurrent.Lazy;

public class LazySortedStorageReaderImpl<PK extends PrimaryKey<PK>, D extends Databean<PK, D>> 
implements LazySortedStorageReader<PK, D>{

	private SortedStorageReader<PK, D> storage;

	public LazySortedStorageReaderImpl(SortedStorageReader<PK, D> storage){
		this.storage = storage;
	}
	
	@Override
	public Lazy<PK> getFirstKey(final Config config){
		return new Lazy<PK>(){
			@Override
			protected PK load(){
				return storage.getFirstKey(config);
			}
		};
	}

	@Override
	public Lazy<D> getFirst(final Config config){
		return new Lazy<D>(){
			@Override
			protected D load(){
				return storage.getFirst(config);
			}
		};
	}

	@Override
	public Lazy<List<D>> getWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config){
		return new Lazy<List<D>>(){
			@Override
			protected List<D> load(){
				return storage.getWithPrefix(prefix, wildcardLastField, config);
			}	
		};
	}

	@Override
	public Lazy<List<D>> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField,
			final Config config){
		return new Lazy<List<D>>(){
			@Override
			protected List<D> load(){
				return storage.getWithPrefixes(prefixes, wildcardLastField, config);
			}	
		};
	}

}
