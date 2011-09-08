package com.hotpads.datarouter.node.type.caching.map.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter.MapStorageWriterNode;
import com.hotpads.datarouter.node.type.caching.map.MapCachingMapStorageNode;
import com.hotpads.datarouter.node.type.caching.map.base.BaseMapCachingNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;

public class MapCachingMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageWriterNode<PK,D>>
implements MapStorageWriter<PK,D>{
	
	public static Config CACHE_CONFIG = MapCachingMapStorageNode.CACHE_CONFIG;
	
	protected BaseMapCachingNode<PK,D,F,N> target;
	protected boolean cacheWrites = false;
	
	public MapCachingMapStorageWriterMixin(BaseMapCachingNode<PK,D,F,N> target, boolean cacheWrites){
		this.target = target;
		this.cacheWrites = cacheWrites;
	}

	@Override
	public void delete(PK key, Config config){
		if(BaseMapCachingNode.useCache(config)){
			target.updateLastAttemptedContact();
			target.getCachingNode().delete(key, CACHE_CONFIG);
			target.updateLastContact();
		}
		target.getBackingNode().delete(key, config);
	}

	@Override
	public void deleteAll(Config config){
		if(BaseMapCachingNode.useCache(config)){
			target.updateLastAttemptedContact();
			target.getCachingNode().deleteAll(CACHE_CONFIG);
			target.updateLastContact();
		}
		target.getBackingNode().deleteAll(config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		if(BaseMapCachingNode.useCache(config)){
			target.updateLastAttemptedContact();
			target.getCachingNode().deleteMulti(keys, CACHE_CONFIG);
			target.updateLastContact();
		}
		target.getBackingNode().deleteMulti(keys, config);
	}

	@Override
	public void put(D databean, Config config){
		if(databean==null || databean.getKey()==null){ return; }
		if(BaseMapCachingNode.useCache(config)){
			target.updateLastAttemptedContact();
			if(cacheWrites){
				target.getCachingNode().put(databean, CACHE_CONFIG);
			}else{//TODO check config for ignoring caching
				target.getCachingNode().delete(databean.getKey(), CACHE_CONFIG);
			}
			target.updateLastContact();
		}
		target.getBackingNode().put(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		if(CollectionTool.isEmpty(databeans)){ return; }
		if(BaseMapCachingNode.useCache(config)){
			target.updateLastAttemptedContact();
			if(cacheWrites){
				target.getCachingNode().putMulti(databeans, CACHE_CONFIG);
			}else{//TODO check config for ignoring caching
				target.getCachingNode().deleteMulti(KeyTool.getKeys(databeans), CACHE_CONFIG);
			}
			target.updateLastContact();
		}
		target.getBackingNode().putMulti(databeans, config);
	}
	
}
