package com.hotpads.datarouter.node.adapter.callsite.physical;

import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.callsite.BaseCallsiteAdapter;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public /*abstract*/ class BaseCallsitePhysicalAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalNode<PK,D>>
extends BaseCallsiteAdapter<PK,D,F,N>
implements PhysicalAdapterMixin<PK,D,N>{

	protected final N backingNode;

	public BaseCallsitePhysicalAdapter(NodeParams<PK,D,F> params, N backingNode){
		super(params, backingNode);
		this.backingNode = backingNode;
	}

}
