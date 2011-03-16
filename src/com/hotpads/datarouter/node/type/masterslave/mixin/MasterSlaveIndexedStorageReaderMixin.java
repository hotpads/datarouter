package com.hotpads.datarouter.node.type.masterslave.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.masterslave.BaseMasterSlaveNode;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader.IndexedStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public class MasterSlaveIndexedStorageReaderMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedStorageReaderNode<PK,D>>
implements IndexedStorageReader<PK,D>{
	
	protected BaseMasterSlaveNode<PK,D,N> target;
	
	public MasterSlaveIndexedStorageReaderMixin(BaseMasterSlaveNode<PK,D,N> target){
		this.target = target;
	}

	
	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? target.chooseSlave(config) : target.getMaster();
		return node.lookupUnique(uniqueKey, config);
	}
	
	
	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? target.chooseSlave(config) : target.getMaster();
		return node.lookupMultiUnique(uniqueKeys, config);
	}

	
	@Override
	public List<D> lookup(Lookup<PK> lookup, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? target.chooseSlave(config) : target.getMaster();
		return node.lookup(lookup, config);
	}
	
	
	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? target.chooseSlave(config) : target.getMaster();
		return node.lookup(lookups, config);
	}
	
}
