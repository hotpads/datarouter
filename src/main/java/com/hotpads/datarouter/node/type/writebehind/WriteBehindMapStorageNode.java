package com.hotpads.datarouter.node.type.writebehind;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindMapStorageWriterMixin;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class WriteBehindMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
extends WriteBehindMapStorageReaderNode<PK,D,N>
implements MapStorageNode<PK,D>{

	protected WriteBehindMapStorageWriterMixin<PK,D,N> mixinMapWriteOps;
	
	public WriteBehindMapStorageNode(Class<D> databeanClass, DataRouter router,
			N backingNode, ExecutorService writeExecutor, ScheduledExecutorService cancelExecutor) {
		super(databeanClass, router, backingNode, writeExecutor, cancelExecutor);
		this.mixinMapWriteOps = new WriteBehindMapStorageWriterMixin<PK,D,N>(this);
	}
	
	
	/***************************** MapStorageWriter ****************************/

	@Override
	public void delete(final PK key, final Config config) {
		mixinMapWriteOps.delete(key, config);
	}

	
	@Override
	public void deleteAll(final Config config) {
		mixinMapWriteOps.deleteAll(config);
	}

	
	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config) {
		mixinMapWriteOps.deleteMulti(keys, config);
	}

	
	@Override
	public void put(final D databean, final Config config) {
		mixinMapWriteOps.put(databean, config);
	}

	
	@Override
	public void putMulti(final Collection<D> databeans, final Config config) {
		mixinMapWriteOps.putMulti(databeans, config);
	}
	
	
}
