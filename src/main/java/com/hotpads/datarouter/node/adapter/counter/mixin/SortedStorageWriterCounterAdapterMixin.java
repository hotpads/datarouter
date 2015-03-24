package com.hotpads.datarouter.node.adapter.counter.mixin;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.formatter.NodeCounterFormatter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter.SortedStorageWriterNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class SortedStorageWriterCounterAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedStorageWriterNode<PK,D>>
implements SortedStorageWriter<PK,D>{

	private final NodeCounterFormatter<PK,D,F,N> counter;
	private final N backingNode;
	
	
	public SortedStorageWriterCounterAdapterMixin(NodeCounterFormatter<PK,D,F,N> counter, N backingNode){
		this.counter = counter;
		this.backingNode = backingNode;
	}
	

	/***************** SortedStorageWriter ************************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config pConfig){
		String opName = SortedStorageWriter.OP_deleteRangeWithPrefix;
		counter.count(opName);
		backingNode.deleteRangeWithPrefix(prefix, wildcardLastField, pConfig);
	}
	
	
}
