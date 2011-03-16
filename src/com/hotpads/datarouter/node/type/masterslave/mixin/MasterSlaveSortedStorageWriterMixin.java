package com.hotpads.datarouter.node.type.masterslave.mixin;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.masterslave.BaseMasterSlaveNode;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter.SortedStorageWriterNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class MasterSlaveSortedStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedStorageWriterNode<PK,D>>
implements SortedStorageWriter<PK,D>{
	
	protected BaseMasterSlaveNode<PK,D,N> target;
	
	public MasterSlaveSortedStorageWriterMixin(BaseMasterSlaveNode<PK,D,N> target){
		this.target = target;
	}

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		target.getMaster().deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}
	
	
}
