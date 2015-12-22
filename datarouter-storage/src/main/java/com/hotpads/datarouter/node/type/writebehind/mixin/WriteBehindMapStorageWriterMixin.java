package com.hotpads.datarouter.node.type.writebehind.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.type.writebehind.WriteBehindNode;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrListTool;

public interface WriteBehindMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
extends MapStorageWriter<PK,D>, WriteBehindNode<PK,D,N>{

	@Override
	public default void delete(PK key, Config config){
		getQueue().offer(new WriteWrapper<>(OP_delete, DrListTool.wrap(key), config));
	}

	@Override
	public default void deleteMulti(Collection<PK> keys, Config config){
		getQueue().offer(new WriteWrapper<>(OP_delete, keys, config));
	}

	@Override
	public default void deleteAll(Config config){
		getQueue().offer(new WriteWrapper<>(OP_deleteAll, DrListTool.wrap(new Object()), config));
	}

	@Override
	public default void put(D databean, Config config) {
		getQueue().offer(new WriteWrapper<>(OP_put, DrListTool.wrap(databean), config));
	}

	@Override
	public default void putMulti(Collection<D> databeans, Config config) {
		getQueue().offer(new WriteWrapper<>(OP_put, databeans, config));
	}

	@SuppressWarnings("unchecked")
	@Override
	public default boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		if(writeWrapper.getOp().equals(OP_put)){
			getBackingNode().putMulti((Collection<D>)writeWrapper.getObjects(), writeWrapper.getConfig());
		}else if(writeWrapper.getOp().equals(OP_delete)){
			getBackingNode().deleteMulti((Collection<PK>)writeWrapper.getObjects(), writeWrapper.getConfig());
		}else if(writeWrapper.getOp().equals(OP_deleteAll)){
			getBackingNode().deleteAll(writeWrapper.getConfig());
		}else{
			return false;
		}
		return true;
	}

}