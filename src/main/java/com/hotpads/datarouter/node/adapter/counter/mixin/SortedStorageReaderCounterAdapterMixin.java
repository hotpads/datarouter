package com.hotpads.datarouter.node.adapter.counter.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.formatter.NodeCounterFormatter;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class SortedStorageReaderCounterAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedStorageReaderNode<PK,D>>
implements SortedStorageReader<PK,D>{

	private final NodeCounterFormatter<PK,D,F,N> counter;
	private final N backingNode;
	
	
	public SortedStorageReaderCounterAdapterMixin(NodeCounterFormatter<PK,D,F,N> counter, N backingNode){
		this.counter = counter;
		this.backingNode = backingNode;
	}


	/***************** SortedStorageReader ************************************/
	
	@Override
	public D getFirst(Config pConfig){
		String opName = SortedStorageReader.OP_getFirst;
		counter.count(opName);
		D result = backingNode.getFirst(pConfig);
		String hitOrMiss = result != null ? "hit" : "miss";
		counter.count(opName + " " + hitOrMiss);
		return result;
	}

	@Override
	public PK getFirstKey(Config pConfig){
		String opName = SortedStorageReader.OP_getFirstKey;
		counter.count(opName);
		PK result = backingNode.getFirstKey(pConfig);
		String hitOrMiss = result != null ? "hit" : "miss";
		counter.count(opName + " " + hitOrMiss);
		return result;
	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config pConfig){
		String opName = SortedStorageReader.OP_getWithPrefix;
		counter.count(opName);
		List<D> results = backingNode.getWithPrefix(prefix, wildcardLastField, pConfig);
		counter.count(opName + " databeans", DrCollectionTool.size(results));
		return results;
	}

	@Override
	public List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config pConfig){
		String opName = SortedStorageReader.OP_getWithPrefixes;
		counter.count(opName);
		counter.count(opName + " prefixes", DrCollectionTool.size(prefixes));
		List<D> results = backingNode.getWithPrefixes(prefixes, wildcardLastField, pConfig);
		counter.count(opName + " databeans", DrCollectionTool.size(results));
		//record hits and misses?
		return results;
	}
	
	@Override
	public SortedScannerIterable<PK> scanKeys(Range<PK> range, Config pConfig){
		String opName = SortedStorageReader.OP_scanKeys;
		counter.count(opName);
		return backingNode.scanKeys(range, pConfig);
	}
	
	@Override
	public SortedScannerIterable<D> scan(Range<PK> range, Config pConfig){
		String opName = SortedStorageReader.OP_scan;
		counter.count(opName);
		return backingNode.scan(range, pConfig);
	}
}
