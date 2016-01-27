package com.hotpads.datarouter.node.adapter.callsite;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.mixin.MapStorageWriterCallsiteAdapterMixin;
import com.hotpads.datarouter.node.adapter.callsite.mixin.SortedStorageWriterCallsiteAdapterMixin;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class SortedMapStorageCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageNode<PK,D>>
extends SortedMapStorageReaderCallsiteAdapter<PK,D,F,N>
implements SortedMapStorageNode<PK,D>, MapStorageWriterCallsiteAdapterMixin<PK,D,N>{

	private SortedStorageWriterCallsiteAdapterMixin<PK,D,F,N> sortedStorageWriterMixin;

	public SortedMapStorageCallsiteAdapter(NodeParams<PK,D,F> params, N backingNode){
		super(params, backingNode);
		this.sortedStorageWriterMixin = new SortedStorageWriterCallsiteAdapterMixin<>(this, backingNode);
	}

	/***************** SortedStorageWriter ************************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		sortedStorageWriterMixin.deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}

}
