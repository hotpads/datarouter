package com.hotpads.datarouter.node.adapter.counter.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.formatter.NodeCounterFormatter;
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
	
	private final NodeCounterFormatter<PK,D,F,N> counter;
	private final N backingNode;
	
	
	public MapStorageReaderCounterAdapterMixin(NodeCounterFormatter<PK,D,F,N> counter, N backingNode){
		this.counter = counter;
		this.backingNode = backingNode;
	}


	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(PK key, Config pConfig){
		String opName = MapStorageReader.OP_exists;
		counter.count(opName);
		boolean result = backingNode.exists(key, pConfig);
		String hitOrMiss = result ? "hit" : "miss";
		counter.count(opName + " " + hitOrMiss);
		return result;
	}

	@Override
	public D get(PK key, Config pConfig){
		String opName = MapStorageReader.OP_get;
		counter.count(opName);
		D result = backingNode.get(key, pConfig);
		String hitOrMiss = result != null ? "hit" : "miss";
		counter.count(opName + " " + hitOrMiss);
		return result;
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config pConfig){
		String opName = MapStorageReader.OP_getMulti;
		counter.count(opName);
		List<D> results = backingNode.getMulti(keys, pConfig);
		int numHits = DrCollectionTool.size(results);
		int numMisses = DrCollectionTool.size(keys) - numHits;
		counter.count(opName + " hit", numHits);
		counter.count(opName + " miss", numMisses);
		return results;
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config pConfig){
		String opName = MapStorageReader.OP_getKeys;
		counter.count(opName);
		List<PK> results = backingNode.getKeys(keys, pConfig);
		int numHits = DrCollectionTool.size(results);
		int numMisses = DrCollectionTool.size(keys) - numHits;
		counter.count(opName + " hit", numHits);
		counter.count(opName + " miss", numMisses);
		return results;
	}
	
}
