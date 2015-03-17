package com.hotpads.datarouter.node.adapter.counter.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.formatter.NodeCounterFormatter;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader.IndexedStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class IndexedStorageReaderCounterAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedStorageReaderNode<PK,D>>
implements IndexedStorageReader<PK,D>{

	private final NodeCounterFormatter<PK,D,F,N> counter;
	private final N backingNode;
	
	
	public IndexedStorageReaderCounterAdapterMixin(NodeCounterFormatter<PK,D,F,N> counter, N backingNode){
		this.counter = counter;
		this.backingNode = backingNode;
	}
	

	/***************** IndexedSortedMapStorageReader ************************************/

	@Override
	public Long count(Lookup<PK> lookup, Config pConfig){
		String opName = IndexedStorageReader.OP_count;
		counter.count(opName);
		Long result = backingNode.count(lookup, pConfig);
		counter.count(opName + " total", result);
		if(result == 0){
			counter.count(opName + " zeros", result);
		}
		return result;
	}

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config pConfig){
		String opName = IndexedStorageReader.OP_lookupUnique;
		counter.count(opName);
		D result = backingNode.lookupUnique(uniqueKey, pConfig);
		String hitOrMiss = result != null ? "hit" : "miss";
		counter.count(opName + " " + hitOrMiss);
		return result;
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config pConfig){
		String opName = IndexedStorageReader.OP_lookupMultiUnique;
		counter.count(opName);
		List<D> results = backingNode.lookupMultiUnique(uniqueKeys, pConfig);
		int numAttempts = DrCollectionTool.size(uniqueKeys);
		int numHits = DrCollectionTool.size(results);
		int numMisses = numAttempts - numHits;
		counter.count(opName + " attempts", numAttempts);
		counter.count(opName + " hits", numHits);
		counter.count(opName + " misses", numMisses);
		return results;
	}

	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config pConfig){
		String opName = IndexedStorageReader.OP_lookup;
		counter.count(opName);
		List<D> results = backingNode.lookup(lookup, wildcardLastField, pConfig);
		int numRows = DrCollectionTool.size(results);
		counter.count(opName + " rows", numRows);
		if(numRows == 0){
			counter.count(opName + " misses");
		}
		return results;
	}

	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config pConfig){
		String opName = IndexedStorageReader.OP_lookupMulti;
		counter.count(opName);
		List<D> results = backingNode.lookup(lookups, pConfig);
		int numRows = DrCollectionTool.size(results);
		counter.count(opName + " rows", numRows);
		if(numRows == 0){
			counter.count(opName + " misses");
		}
		return results;
	}
	
}
