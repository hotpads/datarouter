package com.hotpads.datarouter.node.type.writebehind;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindMapStorageWriterMixin;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindSortedStorageWriterMixin;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public class WriteBehindIndexedSortedMapStorageNode <
	PK extends PrimaryKey<PK>,
	D extends Databean<PK, D>,
	N extends IndexedSortedMapStorageNode<PK, D>>
extends WriteBehindIndexedMapStorageReaderNode<PK, D, N>
implements IndexedSortedMapStorageNode<PK, D> {
	
	protected WriteBehindMapStorageWriterMixin<PK,D,N> mixinMapWriteOps;
	protected WriteBehindSortedStorageWriterMixin<PK,D,N> mixinSortedWriteOps;
	protected WriteBehindIndexedStorageWriterMixin<PK,D,N> mixinIndexedWriteOps;
	
	public WriteBehindIndexedSortedMapStorageNode(Class<D> databeanClass, DataRouter router, N backingNode,
			ExecutorService writeExecutor, ScheduledExecutorService cancelExecutor) {
		super(databeanClass, router, backingNode, writeExecutor, cancelExecutor);
		mixinMapWriteOps = new WriteBehindMapStorageWriterMixin<PK,D,N>(this);
		mixinSortedWriteOps = new WriteBehindSortedStorageWriterMixin<PK,D,N>(this);
		mixinIndexedWriteOps = new WriteBehindIndexedStorageWriterMixin<PK,D,N>(this);
	}

	@Override
	public void put(D databean, Config config) {
		mixinMapWriteOps.put(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		mixinMapWriteOps.putMulti(databeans, config);
	}

	@Override
	public void delete(PK key, Config config) {
		mixinMapWriteOps.delete(key, config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config) {
		mixinMapWriteOps.deleteMulti(keys, config);
	}

	@Override
	public void deleteAll(Config config) {
		mixinMapWriteOps.deleteAll(config);
	}

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		mixinSortedWriteOps.deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}

	@Override
	public void delete(Lookup<PK> lookup, Config config) {
		mixinIndexedWriteOps.delete(lookup, config);
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config) {
		mixinIndexedWriteOps.deleteUnique(uniqueKey, config);
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		mixinIndexedWriteOps.deleteMultiUnique(uniqueKeys, config);
	}
}
