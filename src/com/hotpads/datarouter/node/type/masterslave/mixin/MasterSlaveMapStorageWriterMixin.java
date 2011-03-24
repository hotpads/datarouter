package com.hotpads.datarouter.node.type.masterslave.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter.MapStorageWriterNode;
import com.hotpads.datarouter.node.type.masterslave.base.BaseMasterSlaveNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class MasterSlaveMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageWriterNode<PK,D>>
implements MapStorageWriter<PK,D>{
	
	protected BaseMasterSlaveNode<PK,D,N> target;
	
	public MasterSlaveMapStorageWriterMixin(BaseMasterSlaveNode<PK,D,N> target){
		this.target = target;
	}

	@Override
	public void delete(PK key, Config config) {
		target.getMaster().delete(key, config);
	}

	@Override
	public void deleteAll(Config config) {
		target.getMaster().deleteAll(config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config) {
		target.getMaster().deleteMulti(keys, config);
	}

	@Override
	public void put(D databean, Config config) {
		target.getMaster().put(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		target.getMaster().putMulti(databeans, config);
	}

	
	
}
