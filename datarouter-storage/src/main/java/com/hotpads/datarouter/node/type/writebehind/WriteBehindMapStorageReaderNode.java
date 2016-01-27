package com.hotpads.datarouter.node.type.writebehind;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.node.type.writebehind.base.BaseWriteBehindNode;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class WriteBehindMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageReaderNode<PK,D>>
extends BaseWriteBehindNode<PK,D,N>
implements MapStorageReaderNode<PK,D>{

	public WriteBehindMapStorageReaderNode(Supplier<D> databeanSupplier, Router router, N backingNode){
		super(databeanSupplier, router, backingNode);
	}

	@Override
	public boolean exists(PK key, Config config){
		return backingNode.exists(key, config);
	}

	@Override
	public D get(PK key, Config config) {
		return backingNode.get(key, config);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config) {
		return backingNode.getMulti(keys, config);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config) {
		return backingNode.getKeys(keys, config);
	}

	@Override
	public boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		return false;
	}

}
