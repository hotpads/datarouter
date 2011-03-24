package com.hotpads.datarouter.node.type.masterslave.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter.IndexedStorageWriterNode;
import com.hotpads.datarouter.node.type.masterslave.base.BaseMasterSlaveNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public class MasterSlaveIndexedStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedStorageWriterNode<PK,D>>
implements IndexedStorageWriter<PK,D>{
	
	protected BaseMasterSlaveNode<PK,D,N> target;
	
	public MasterSlaveIndexedStorageWriterMixin(BaseMasterSlaveNode<PK,D,N> target){
		this.target = target;
	}
	
	
	@Override
	public void delete(Lookup<PK> lookup, Config config) {
		target.getMaster().delete(lookup, config);
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config) {
		target.getMaster().deleteUnique(uniqueKey, config);
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		target.getMaster().deleteMultiUnique(uniqueKeys, config);
	}
	
	
}
