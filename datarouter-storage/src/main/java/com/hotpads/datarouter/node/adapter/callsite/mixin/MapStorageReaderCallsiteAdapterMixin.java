package com.hotpads.datarouter.node.adapter.callsite.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.callsite.CallsiteAdapter;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface MapStorageReaderCallsiteAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageReaderNode<PK,D>>
extends MapStorageReader<PK,D>, CallsiteAdapter{

	N getBackingNode();

	@Override
	default boolean exists(PK key, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		boolean result = false;
		try{
			result = getBackingNode().exists(key, config);
			return result;
		}finally{
			int numResults = result ? 1 : 0;
			recordCallsite(config, startNs, numResults);
		}
	}

	@Override
	default D get(PK key, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		D result = null;
		try{
			result = getBackingNode().get(key, config);
			return result;
		}finally{
			int numResults = result == null ? 0 : 1;
			recordCallsite(config, startNs, numResults);
		}
	}

	@Override
	default List<D> getMulti(Collection<PK> keys, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		List<D> results = null;
		try{
			results = getBackingNode().getMulti(keys, config);
			return results;
		}finally{
			recordCollectionCallsite(config, startNs, results);
		}
	}

	@Override
	default List<PK> getKeys(Collection<PK> keys, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		List<PK> results = null;
		try{
			results = getBackingNode().getKeys(keys, config);
			return results;
		}finally{
			recordCollectionCallsite(config, startNs, results);
		}
	}

}
