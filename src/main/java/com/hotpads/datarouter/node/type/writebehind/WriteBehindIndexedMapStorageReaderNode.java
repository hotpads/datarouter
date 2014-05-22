package com.hotpads.datarouter.node.type.writebehind;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.IndexedSortedMapStorageReader.IndexedSortedMapStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public class WriteBehindIndexedMapStorageReaderNode <
	PK extends PrimaryKey<PK>,
	D extends Databean<PK, D>,
	N extends IndexedSortedMapStorageReaderNode<PK, D>>
extends WriteBehindSortedMapStorageReaderNode<PK, D, N>
implements IndexedSortedMapStorageReaderNode<PK, D> {

	public WriteBehindIndexedMapStorageReaderNode(Class<D> databeanClass, DataRouter router, N backingNode,
			ExecutorService writeExecutor, ScheduledExecutorService cancelExecutor) {
		super(databeanClass, router, backingNode, writeExecutor, cancelExecutor);
	}

	@Override
	public Long count(Lookup<PK> lookup, Config config) {
		return backingNode.count(lookup, config);
	}

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config) {
		return backingNode.lookupUnique(uniqueKey, config);
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		return backingNode.lookupMultiUnique(uniqueKeys, config);
	}

	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config) {
		return backingNode.lookup(lookup, wildcardLastField, config);
	}

	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookup, Config config) {
		return backingNode.lookup(lookup, config);
	}

}
