package com.hotpads.datarouter.node.adapter.callsite.physical;

import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.callsite.BaseCallsiteAdapter;
import com.hotpads.datarouter.node.adapter.callsite.mixin.MapStorageReaderCallsiteAdapterMixin;
import com.hotpads.datarouter.node.adapter.callsite.mixin.MapStorageWriterCallsiteAdapterMixin;
import com.hotpads.datarouter.node.adapter.callsite.mixin.SortedStorageReaderCallsiteAdapterMixin;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class PhysicalSortedMapStorageCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalSortedMapStorageNode<PK,D>>
extends BaseCallsiteAdapter<PK,D,F,N>
implements PhysicalSortedMapStorageNode<PK,D>,
		MapStorageWriterCallsiteAdapterMixin<PK,D,N>,
		SortedStorageReaderCallsiteAdapterMixin<PK,D,N>,
		MapStorageReaderCallsiteAdapterMixin<PK,D,N>,
		PhysicalAdapterMixin<PK,D,N>{

	public PhysicalSortedMapStorageCallsiteAdapter(NodeParams<PK,D,F> params, N backingNode){
		super(params, backingNode);
	}

}
