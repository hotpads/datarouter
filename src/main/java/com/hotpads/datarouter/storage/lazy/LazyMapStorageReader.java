package com.hotpads.datarouter.storage.lazy;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.concurrent.Lazy;

public class LazyMapStorageReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>{
	
	private MapStorageReader<PK, D> mapStorage;

	public LazyMapStorageReader(MapStorageReader<PK, D> mapStorage){
		this.mapStorage = mapStorage;
	}

	public Lazy<D> get(final PK key, final Config config){
		return new Lazy<D>(){

			@Override
			protected D load(){
				return mapStorage.get(key, config);
			}

		};
	}

	public Lazy<List<D>> getMulti(final Collection<PK> key, final Config config){
		return new Lazy<List<D>>(){

			@Override
			protected List<D> load(){
				return mapStorage.getMulti(key, config);
			}
			
		};
	}

}
