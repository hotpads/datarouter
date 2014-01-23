package com.hotpads.datarouter.node.type.masterslave.mixin;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter.SortedStorageWriterNode;
import com.hotpads.datarouter.node.type.masterslave.base.BaseMasterSlaveNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class MasterSlaveSortedStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedStorageWriterNode<PK,D>>
implements SortedStorageWriter<PK,D>{
	
	protected BaseMasterSlaveNode<PK,D,F,N> target;
	
	public MasterSlaveSortedStorageWriterMixin(BaseMasterSlaveNode<PK,D,F,N> target){
		this.target = target;
	}

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		target.getMaster().deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}
	
	
}
