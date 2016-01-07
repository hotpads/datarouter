package com.hotpads.datarouter.node.adapter.callsite.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.callsite.CallsiteAdapter;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface MapStorageWriterCallsiteAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
extends MapStorageWriter<PK,D>, CallsiteAdapter{

	N getBackingNode();

	@Override
	public default void put(D databean, Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			getBackingNode().put(databean, nullSafeConfig);
		}finally{
			recordCallsite(nullSafeConfig, startNs, 1);
		}
	}

	@Override
	public default void putMulti(Collection<D> databeans, Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			getBackingNode().putMulti(databeans, nullSafeConfig);
		}finally{
			recordCollectionCallsite(nullSafeConfig, startNs, databeans);
		}
	}

	@Override
	public default void delete(PK key, Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			getBackingNode().delete(key, nullSafeConfig);
		}finally{
			recordCallsite(nullSafeConfig, startNs, 1);
		}
	}

	@Override
	public default void deleteMulti(Collection<PK> keys, Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			getBackingNode().deleteMulti(keys, nullSafeConfig);
		}finally{
			recordCollectionCallsite(nullSafeConfig, startNs, keys);
		}
	}

	@Override
	public default void deleteAll(Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			getBackingNode().deleteAll(nullSafeConfig);
		}finally{
			recordCallsite(nullSafeConfig, startNs, 0);
		}
	}

}
