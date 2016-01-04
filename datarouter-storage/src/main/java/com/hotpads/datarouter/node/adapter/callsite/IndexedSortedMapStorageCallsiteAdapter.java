package com.hotpads.datarouter.node.adapter.callsite;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.mixin.IndexedStorageWriterCallsiteAdapterMixin;
import com.hotpads.datarouter.node.adapter.callsite.mixin.MapStorageWriterCallsiteAdapterMixin;
import com.hotpads.datarouter.node.adapter.callsite.mixin.SortedStorageWriterCallsiteAdapterMixin;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public class IndexedSortedMapStorageCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedSortedMapStorageNode<PK,D>>
extends IndexedSortedMapStorageReaderCallsiteAdapter<PK,D,F,N>
implements IndexedSortedMapStorageNode<PK,D>, MapStorageWriterCallsiteAdapterMixin<PK,D,N>{

	private SortedStorageWriterCallsiteAdapterMixin<PK,D,F,N> sortedStorageWriterMixin;
	private IndexedStorageWriterCallsiteAdapterMixin<PK,D,F,N> indexedStorageWriterMixin;


	public IndexedSortedMapStorageCallsiteAdapter(NodeParams<PK,D,F> params, N backingNode){
		super(params, backingNode);
		this.sortedStorageWriterMixin = new SortedStorageWriterCallsiteAdapterMixin<>(this, backingNode);
		this.indexedStorageWriterMixin = new IndexedStorageWriterCallsiteAdapterMixin<>(this, backingNode);
	}

	/***************** SortedMapStorage ************************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		sortedStorageWriterMixin.deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}


	/***************** IndexedSortedMapStorage ************************************/

	@Override
	public void delete(Lookup<PK> lookup, Config config){
		indexedStorageWriterMixin.delete(lookup, config);
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		indexedStorageWriterMixin.deleteUnique(uniqueKey, config);
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		indexedStorageWriterMixin.deleteMultiUnique(uniqueKeys, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		indexedStorageWriterMixin.deleteByIndex(keys, config);
	}
}
