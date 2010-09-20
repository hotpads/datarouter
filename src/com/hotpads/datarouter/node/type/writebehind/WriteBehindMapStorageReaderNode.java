package com.hotpads.datarouter.node.type.writebehind;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.writebehind.BaseWriteBehindNode;
import com.hotpads.datarouter.node.op.MapStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class WriteBehindMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		N extends MapStorageReaderNode<PK,D>>
extends BaseWriteBehindNode<PK,D,N>
implements MapStorageReaderNode<PK,D>{
	
	public WriteBehindMapStorageReaderNode(Class<D> databeanClass, DataRouter router,
			N backingNode, ExecutorService writeExecutor, ScheduledExecutorService cancelExecutor) {
		super(databeanClass, router, backingNode, writeExecutor, cancelExecutor);
	}

	
	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(PK key, Config config){
		return backingNode.exists(key, config);
	}

	@Override
	public D get(PK key, Config config) {
		return backingNode.get(key, config);
	}

	@Override
	public List<D> getAll(Config config) {
		return backingNode.getAll(config);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config) {
		return backingNode.getMulti(keys, config);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config) {
		return backingNode.getKeys(keys, config);
	}

	
	
}
