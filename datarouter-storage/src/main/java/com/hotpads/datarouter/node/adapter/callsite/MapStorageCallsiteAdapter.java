package com.hotpads.datarouter.node.adapter.callsite;

import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.mixin.MapStorageWriterCallsiteAdapterMixin;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class MapStorageCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageNode<PK,D>>
extends MapStorageReaderCallsiteAdapter<PK,D,F,N>
implements MapStorageNode<PK,D>, MapStorageWriterCallsiteAdapterMixin<PK,D,N>{

	public MapStorageCallsiteAdapter(NodeParams<PK,D,F> params, N backingNode){
		super(params, backingNode);
	}

}
