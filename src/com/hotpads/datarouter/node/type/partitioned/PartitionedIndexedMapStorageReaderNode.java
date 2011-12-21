package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.IndexedMapStorageReader.IndexedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.combo.reader.IndexedMapStorageReader.PhysicalIndexedMapStorageReaderNode;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedIndexedStorageReaderMixin;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public abstract class PartitionedIndexedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalIndexedMapStorageReaderNode<PK,D>>
extends PartitionedMapStorageReaderNode<PK,D,F,N>
implements IndexedMapStorageReaderNode<PK,D>{

	protected PartitionedIndexedStorageReaderMixin<PK,D,F,N> mixinIndexedReadOps;
	
	public PartitionedIndexedMapStorageReaderNode(Class<D> databeanClass, Class<F> fielderClass, DataRouter router) {
		super(databeanClass, fielderClass, router);
		this.mixinIndexedReadOps = new PartitionedIndexedStorageReaderMixin<PK,D,F,N>(this);
	}


	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config) {
		return mixinIndexedReadOps.lookupUnique(uniqueKey, config);
	}

	
	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		return mixinIndexedReadOps.lookupMultiUnique(uniqueKeys, config);
	}

	
	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config) {
		return mixinIndexedReadOps.lookup(lookup, wildcardLastField, config);
	}
	
	
	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config config) {
		return mixinIndexedReadOps.lookup(lookups, config);
	}
	
}
