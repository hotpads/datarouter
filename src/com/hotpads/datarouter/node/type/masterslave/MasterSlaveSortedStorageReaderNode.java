package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.SortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;

public class MasterSlaveSortedStorageReaderNode<D extends Databean,N extends SortedStorageReaderNode<D>>
extends MasterSlaveMapStorageReaderNode<D,N>
implements SortedStorageReaderNode<D>{
	
	public MasterSlaveSortedStorageReaderNode(
			Class<D> databeanClass, DataRouter router,
			N master, Collection<N> slaves) {
		
		super(databeanClass, router, master, slaves);
	}
	
	public MasterSlaveSortedStorageReaderNode(
			Class<D> databeanClass, DataRouter router) {
		
		super(databeanClass, router);
	}

	/***************** SortedStorageReader ************************************/
	
	@Override
	public D getFirst(Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getFirst(config);
	}

	@Override
	public List<D> getPrefixedRange(
			Key<D> prefix, boolean wildcardLastField,
			Key<D> start, boolean startInclusive, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getPrefixedRange(
				prefix, wildcardLastField, start, startInclusive, config);
	}

	@Override
	public List<D> getRange(Key<D> start, boolean startInclusive, Key<D> end,
			boolean endInclusive, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getWithPrefix(Key<D> prefix, boolean wildcardLastField, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getWithPrefix(prefix,wildcardLastField, config);
	}

	@Override
	public List<D> getWithPrefixes(Collection<? extends Key<D>> prefixes, boolean wildcardLastField, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getWithPrefixes(prefixes,wildcardLastField, config);
	}
	
	
	
}
