package com.hotpads.datarouter.node.adapter.counter.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public interface MapStorageCounterAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
extends MapStorage<PK,D>, MapStorageReaderCounterAdapterMixin<PK,D,N>{

	//Writer

	@Override
	public default void put(D databean, Config config){
		String opName = MapStorageWriter.OP_put;
		getCounter().count(opName);
		getBackingNode().put(databean, config);
	}

	@Override
	public default void putMulti(Collection<D> databeans, Config config){
		String opName = MapStorageWriter.OP_putMulti;
		getCounter().count(opName);
		getBackingNode().putMulti(databeans, config);
		getCounter().count(opName + " databeans", DrCollectionTool.size(databeans));
	}

	@Override
	public default void delete(PK key, Config config){
		String opName = MapStorageWriter.OP_delete;
		getCounter().count(opName);
		getBackingNode().delete(key, config);
	}

	@Override
	public default void deleteMulti(Collection<PK> keys, Config config){
		String opName = MapStorageWriter.OP_deleteMulti;
		getCounter().count(opName);
		getBackingNode().deleteMulti(keys, config);
		getCounter().count(opName + " keys", DrCollectionTool.size(keys));
	}

	@Override
	public default void deleteAll(Config config){
		String opName = MapStorageWriter.OP_deleteAll;
		getCounter().count(opName);
		getBackingNode().deleteAll(config);
	}

}
