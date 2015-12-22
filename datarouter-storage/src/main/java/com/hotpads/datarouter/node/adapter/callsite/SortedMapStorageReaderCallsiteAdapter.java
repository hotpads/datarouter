package com.hotpads.datarouter.node.adapter.callsite;

import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.mixin.SortedStorageReaderCallsiteAdapterMixin;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class SortedMapStorageReaderCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageReaderNode<PK,D>>
extends MapStorageReaderCallsiteAdapter<PK,D,F,N>
implements SortedStorageReaderNode<PK,D>, SortedStorageReaderCallsiteAdapterMixin<PK,D,N>{

	public SortedMapStorageReaderCallsiteAdapter(NodeParams<PK,D,F> params, N backingNode){
		super(params, backingNode);
	}

}
