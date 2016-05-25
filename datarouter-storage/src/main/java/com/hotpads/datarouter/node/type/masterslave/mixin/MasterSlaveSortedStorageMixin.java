package com.hotpads.datarouter.node.type.masterslave.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage.SortedStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public interface MasterSlaveSortedStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedStorageNode<PK,D>>
extends MasterSlaveNode<PK,D,N>, SortedStorage<PK,D>{

	@Override
	@Deprecated
	default List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.getWithPrefix(prefix,wildcardLastField, config);
	}

	@Override
	@Deprecated
	default List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.getWithPrefixes(prefixes, wildcardLastField, config);
	}

	@Override
	default Iterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.scanKeysMulti(ranges, config);
	}

	@Override
	default Iterable<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.scanMulti(ranges, config);
	}
}
