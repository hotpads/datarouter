package com.hotpads.datarouter.node.adapter.counter.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.CounterAdapter;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public interface MapStorageReaderCounterAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageReaderNode<PK,D>>
extends MapStorageReader<PK,D>, CounterAdapter<PK,D,N>{

	@Override
	public default boolean exists(PK key, Config config){
		String opName = MapStorageReader.OP_exists;
		getCounter().count(opName);
		boolean result = getBackingNode().exists(key, config);
		String hitOrMiss = result ? "hit" : "miss";
		getCounter().count(opName + " " + hitOrMiss);
		return result;
	}

	@Override
	public default D get(PK key, Config config){
		String opName = MapStorageReader.OP_get;
		getCounter().count(opName);
		D result = getBackingNode().get(key, config);
		String hitOrMiss = result != null ? "hit" : "miss";
		getCounter().count(opName + " " + hitOrMiss);
		return result;
	}

	@Override
	public default List<D> getMulti(Collection<PK> keys, Config config){
		String opName = MapStorageReader.OP_getMulti;
		getCounter().count(opName);
		List<D> results = getBackingNode().getMulti(keys, config);
		int numHits = DrCollectionTool.size(results);
		int numMisses = DrCollectionTool.size(keys) - numHits;
		getCounter().count(opName + " hit", numHits);
		getCounter().count(opName + " miss", numMisses);
		return results;
	}

	@Override
	public default List<PK> getKeys(Collection<PK> keys, Config config){
		String opName = MapStorageReader.OP_getKeys;
		getCounter().count(opName);
		List<PK> results = getBackingNode().getKeys(keys, config);
		int numHits = DrCollectionTool.size(results);
		int numMisses = DrCollectionTool.size(keys) - numHits;
		getCounter().count(opName + " hit", numHits);
		getCounter().count(opName + " miss", numMisses);
		return results;
	}

}
