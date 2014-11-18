package com.hotpads.datarouter.node.adapter.mixin;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.BaseAdapterNode;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter.SortedStorageWriterNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class SortedStorageWriterAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedStorageWriterNode<PK,D>>
implements SortedStorageWriter<PK,D>{

	private BaseAdapterNode<PK,D,F,N> adapterNode;
	private N backingNode;
	
	
	public SortedStorageWriterAdapterMixin(BaseAdapterNode<PK,D,F,N> adapterNode, N backingNode){
		this.adapterNode = adapterNode;
		this.backingNode = backingNode;
	}
	

	/***************** SortedStorageWriter ************************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			backingNode.deleteRangeWithPrefix(prefix, wildcardLastField, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}
	
	
}
