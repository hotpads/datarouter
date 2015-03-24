package com.hotpads.datarouter.node.adapter.counter.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.formatter.NodeCounterFormatter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter.MapStorageWriterNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class MapStorageWriterCounterAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageWriterNode<PK,D>>
implements MapStorageWriter<PK,D>{

	private final NodeCounterFormatter<PK,D,F,N> counter;
	private N backingNode;
	
	
	public MapStorageWriterCounterAdapterMixin(NodeCounterFormatter<PK,D,F,N> counter, N backingNode){
		this.counter = counter;
		this.backingNode = backingNode;
	}

	
	@Override
	public void put(D databean, Config pConfig){
		String opName = MapStorageWriter.OP_put;
		counter.count(opName);
		backingNode.put(databean, pConfig);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config pConfig){
		String opName = MapStorageWriter.OP_putMulti;
		counter.count(opName);
		backingNode.putMulti(databeans, pConfig);
		counter.count(opName + " databeans", DrCollectionTool.size(databeans));
	}

	@Override
	public void delete(PK key, Config pConfig){
		String opName = MapStorageWriter.OP_delete;
		counter.count(opName);
		backingNode.delete(key, pConfig);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config pConfig){
		String opName = MapStorageWriter.OP_deleteMulti;
		counter.count(opName);
		backingNode.deleteMulti(keys, pConfig);
		counter.count(opName + " keys", DrCollectionTool.size(keys));
	}

	@Override
	public void deleteAll(Config pConfig){
		String opName = MapStorageWriter.OP_deleteAll;
		counter.count(opName);
		backingNode.deleteAll(pConfig);
	}
	
}
