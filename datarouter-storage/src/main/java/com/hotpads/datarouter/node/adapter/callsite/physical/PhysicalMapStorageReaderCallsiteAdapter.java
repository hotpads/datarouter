package com.hotpads.datarouter.node.adapter.callsite.physical;

import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.callsite.MapStorageReaderCallsiteAdapter;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.PhysicalMapStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class PhysicalMapStorageReaderCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		PN extends PhysicalMapStorageReaderNode<PK,D>>
extends MapStorageReaderCallsiteAdapter<PK,D,F,PN>
implements PhysicalMapStorageReaderNode<PK,D>, PhysicalAdapterMixin<PK,D,PN>{


	public PhysicalMapStorageReaderCallsiteAdapter(NodeParams<PK,D,F> params, PN backingNode){
		super(params, backingNode);
	}

}
