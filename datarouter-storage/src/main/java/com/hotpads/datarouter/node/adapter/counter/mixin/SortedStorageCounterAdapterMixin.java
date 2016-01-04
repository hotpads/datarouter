package com.hotpads.datarouter.node.adapter.counter.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.CounterAdapter;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage.SortedStorageNode;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
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
	public default List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		String opName = SortedStorageReader.OP_getWithPrefix;
		getCounter().count(opName);
		List<D> results = getBackingNode().getWithPrefix(prefix, wildcardLastField, config);
		getCounter().count(opName + " databeans", DrCollectionTool.size(results));
		return results;
	}

	@Override
	@Deprecated
	public default List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config){
		String opName = SortedStorageReader.OP_getWithPrefixes;
		getCounter().count(opName);
		getCounter().count(opName + " prefixes", DrCollectionTool.size(prefixes));
		List<D> results = getBackingNode().getWithPrefixes(prefixes, wildcardLastField, config);
		getCounter().count(opName + " databeans", DrCollectionTool.size(results));
		//record hits and misses?
		return results;
	}

	@Override
	public default Iterable<PK> scanKeys(Range<PK> range, Config config){
		String opName = SortedStorageReader.OP_scanKeys;
		getCounter().count(opName);
		return getBackingNode().scanKeys(range, config);
	}

	@Override
	public default Iterable<D> scan(Range<PK> range, Config config){
		String opName = SortedStorageReader.OP_scan;
		getCounter().count(opName);
		return getBackingNode().scan(range, config);
	}

	//Writer

	@Override
	public default void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		String opName = SortedStorageWriter.OP_deleteRangeWithPrefix;
		getCounter().count(opName);
		getBackingNode().deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}
}
