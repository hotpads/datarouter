package com.hotpads.datarouter.node.type.writebehind;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.SortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.iterable.PeekableIterable;

public class WriteBehindSortedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK>,
		N extends SortedStorageReaderNode<PK,D>>
extends WriteBehindMapStorageReaderNode<PK,D,N>
implements SortedStorageReaderNode<PK,D>{
	
	public WriteBehindSortedStorageReaderNode(Class<D> databeanClass, DataRouter router,
			N backingNode, ExecutorService writeExecutor, ScheduledExecutorService cancelExecutor) {
		super(databeanClass, router, backingNode, writeExecutor, cancelExecutor);
	}
	
	
	/***************** SortedStorageReader ************************************/
	
	@Override
	public D getFirst(Config config) {
		return backingNode.getFirst(config);
	}

	@Override
	public PK getFirstKey(Config config) {
		return backingNode.getFirstKey(config);
	}
	
	@Override
	public List<D> getPrefixedRange(
			PK prefix, boolean wildcardLastField,
			PK start, boolean startInclusive, Config config) {
		return backingNode.getPrefixedRange(
				prefix, wildcardLastField, start, startInclusive, config);
	}

	@Override
	public List<PK> getKeysInRange(PK start, boolean startInclusive, PK end,
			boolean endInclusive, Config config) {
		return backingNode.getKeysInRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getRange(PK start, boolean startInclusive, PK end,
			boolean endInclusive, Config config) {
		return backingNode.getRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		return backingNode.getWithPrefix(prefix,wildcardLastField, config);
	}

	@Override
	public List<D> getWithPrefixes(Collection<? extends PK> prefixes, boolean wildcardLastField, Config config) {
		return backingNode.getWithPrefixes(prefixes, wildcardLastField, config);
	}
	
	@Override
	public PeekableIterable<D> scan(PK startKey, boolean startInclusive, PK end, boolean endInclusive, Config config){
		return backingNode.scan(startKey,startInclusive, end, endInclusive, config);
	};
	
}
