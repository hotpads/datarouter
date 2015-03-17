package com.hotpads.datarouter.node.adapter.counter.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.formatter.NodeCounterFormatter;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter.IndexedStorageWriterNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public class IndexedStorageWriterCounterAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedStorageWriterNode<PK,D>>
implements IndexedStorageWriter<PK,D>{

	private final NodeCounterFormatter<PK,D,F,N> counter;
	private final N backingNode;
	
	
	public IndexedStorageWriterCounterAdapterMixin(NodeCounterFormatter<PK,D,F,N> counter, N backingNode){
		this.counter = counter;
		this.backingNode = backingNode;
	}


	/***************** IndexedSortedMapStorage ************************************/

	@Override
	public void delete(Lookup<PK> lookup, Config pConfig){
		String opName = IndexedStorageWriter.OP_indexDelete;
		counter.count(opName);
		backingNode.delete(lookup, pConfig);
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config pConfig){
		String opName = IndexedStorageWriter.OP_deleteUnique;
		counter.count(opName);
		backingNode.deleteUnique(uniqueKey, pConfig);
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config pConfig){
		String opName = IndexedStorageWriter.OP_deleteMultiUnique;
		counter.count(opName);
		backingNode.deleteMultiUnique(uniqueKeys, pConfig);
	}

	
}
