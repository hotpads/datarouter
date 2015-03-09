package com.hotpads.datarouter.node.adapter.counter.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.BaseCounterAdapter;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class MapStorageReaderCounterAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageReaderNode<PK,D>>
implements MapStorageReader<PK,D>{
	
	private BaseCounterAdapter<PK,D,F,N> counterAdapter;
	private N backingNode;
	
	
	public MapStorageReaderCounterAdapterMixin(BaseCounterAdapter<PK,D,F,N> counterAdapter, N backingNode){
		this.counterAdapter = counterAdapter;
		this.backingNode = backingNode;
	}


	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(PK key, Config pConfig){
		String opName = MapStorageReader.OP_exists;
		counterAdapter.count(opName);
		boolean result = backingNode.exists(key, pConfig);
		String hitOrMiss = result ? "hit" : "miss";
		counterAdapter.count(opName + " " + hitOrMiss);
		return result;
	}

	@Override
	public D get(PK key, Config pConfig){
		String opName = MapStorageReader.OP_get;
		counterAdapter.count(opName);
		D result = backingNode.get(key, pConfig);
		String hitOrMiss = result != null ? "hit" : "miss";
		counterAdapter.count(opName + " " + hitOrMiss);
		return result;
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config pConfig){
		String opName = MapStorageReader.OP_getMulti;
		counterAdapter.count(opName);
		List<D> results = backingNode.getMulti(keys, pConfig);
		int numHits = DrCollectionTool.size(results);
		int numMisses = DrCollectionTool.size(keys) - numHits;
		counterAdapter.count(opName + " hit", numHits);
		counterAdapter.count(opName + " miss", numMisses);
		return results;
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config pConfig){
		String opName = MapStorageReader.OP_getKeys;
		counterAdapter.count(opName);
		List<PK> results = backingNode.getKeys(keys, pConfig);
		int numHits = DrCollectionTool.size(results);
		int numMisses = DrCollectionTool.size(keys) - numHits;
		counterAdapter.count(opName + " hit", numHits);
		counterAdapter.count(opName + " miss", numMisses);
		return results;
	}
	
}
