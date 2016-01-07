package com.hotpads.datarouter.node.adapter.callsite.physical;

import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.callsite.SortedMapStorageReaderCallsiteAdapter;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.PhysicalSortedMapStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class PhysicalSortedMapStorageReaderCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		PN extends PhysicalSortedMapStorageReaderNode<PK,D>>
extends SortedMapStorageReaderCallsiteAdapter<PK,D,F,PN>
implements PhysicalSortedMapStorageReaderNode<PK,D>, PhysicalAdapterMixin<PK,D,PN>{


	public PhysicalSortedMapStorageReaderCallsiteAdapter(NodeParams<PK,D,F> params, PN backingNode){
		super(params, backingNode);
	}

}
