package com.hotpads.datarouter.node.adapter.callsite.physical;

import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.callsite.SortedMapStorageCallsiteAdapter;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class PhysicalSortedMapStorageCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		PN extends PhysicalSortedMapStorageNode<PK,D>>
extends SortedMapStorageCallsiteAdapter<PK,D,F,PN>
implements PhysicalSortedMapStorageNode<PK,D>, PhysicalAdapterMixin<PK,D,PN>{

	public PhysicalSortedMapStorageCallsiteAdapter(NodeParams<PK,D,F> params, PN backingNode){
		super(params, backingNode);
	}

}
