package com.hotpads.datarouter.node.adapter.counter.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.CounterAdapter;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage.SortedStorageNode;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.collections.Range;

public interface SortedStorageCounterAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedStorageNode<PK,D>>
extends SortedStorage<PK,D>, CounterAdapter<PK,D,N>{

	//Reader

	@Override
	@Deprecated
	default List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		String opName = SortedStorageReader.OP_getWithPrefix;
		getCounter().count(opName);
		List<D> results = getBackingNode().getWithPrefix(prefix, wildcardLastField, config);
		getCounter().count(opName + " databeans", DrCollectionTool.size(results));
		return results;
	}

	@Override
	@Deprecated
	default List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config){
		String opName = SortedStorageReader.OP_getWithPrefixes;
		getCounter().count(opName);
		getCounter().count(opName + " prefixes", DrCollectionTool.size(prefixes));
		List<D> results = getBackingNode().getWithPrefixes(prefixes, wildcardLastField, config);
		getCounter().count(opName + " databeans", DrCollectionTool.size(results));
		//record hits and misses?
		return results;
	}

	@Override
	default Iterable<PK> scanKeys(Range<PK> range, Config config){
		String opName = SortedStorageReader.OP_scanKeys;
		getCounter().count(opName);
		return getBackingNode().scanKeys(range, config);
	}

	@Override
	default Iterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		String opName = SortedStorageReader.OP_scanKeysMulti;
		getCounter().count(opName);
		return getBackingNode().scanKeysMulti(ranges, config);
	}

	@Override
	default Iterable<D> scan(Range<PK> range, Config config){
		String opName = SortedStorageReader.OP_scan;
		getCounter().count(opName);
		return getBackingNode().scan(range, config);
	}

	@Override
	default Iterable<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		String opName = SortedStorageReader.OP_scanMulti;
		getCounter().count(opName);
		return getBackingNode().scanMulti(ranges, config);
	}

}
