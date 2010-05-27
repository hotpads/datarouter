package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.SortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public class MasterSlaveSortedStorageReaderNode<D extends Databean,PK extends PrimaryKey<D>,N extends SortedStorageReaderNode<D,PK>>
extends MasterSlaveMapStorageReaderNode<D,PK,N>
implements SortedStorageReaderNode<D,PK>{
	
	public MasterSlaveSortedStorageReaderNode(
			Class<PK> primaryKeyClass, DataRouter router,
			N master, Collection<N> slaves) {
		super(primaryKeyClass, router, master, slaves);
	}
	
	public MasterSlaveSortedStorageReaderNode(
			Class<PK> primaryKeyClass, DataRouter router) {
		super(primaryKeyClass, router);
	}

	/***************** SortedStorageReader ************************************/
	
	@Override
	public D getFirst(Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getFirst(config);
	}

	@Override
	public PK getFirstKey(Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getFirstKey(config);
	}
	
	@Override
	public List<D> getPrefixedRange(
			PK prefix, boolean wildcardLastField,
			PK start, boolean startInclusive, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getPrefixedRange(
				prefix, wildcardLastField, start, startInclusive, config);
	}

	@Override
	public List<PK> getKeysInRange(PK start, boolean startInclusive, PK end,
			boolean endInclusive, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getKeysInRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getRange(PK start, boolean startInclusive, PK end,
			boolean endInclusive, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getWithPrefix(prefix,wildcardLastField, config);
	}

	@Override
	public List<D> getWithPrefixes(Collection<? extends PK> prefixes, boolean wildcardLastField, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getWithPrefixes(prefixes,wildcardLastField, config);
	}
	
	
	
}
