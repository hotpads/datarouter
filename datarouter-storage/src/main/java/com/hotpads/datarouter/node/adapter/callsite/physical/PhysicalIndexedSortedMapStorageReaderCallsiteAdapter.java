package com.hotpads.datarouter.node.adapter.callsite.physical;

import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.callsite.IndexedSortedMapStorageReaderCallsiteAdapter;
import com.hotpads.datarouter.node.op.combo.reader.IndexedSortedMapStorageReader.PhysicalIndexedSortedMapStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class PhysicalIndexedSortedMapStorageReaderCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		PN extends PhysicalIndexedSortedMapStorageReaderNode<PK,D>>
extends IndexedSortedMapStorageReaderCallsiteAdapter<PK,D,F,PN>
implements PhysicalIndexedSortedMapStorageReaderNode<PK,D>, PhysicalAdapterMixin<PK,D,PN>{

	public PhysicalIndexedSortedMapStorageReaderCallsiteAdapter(NodeParams<PK,D,F> params, PN backingNode){
		super(params, backingNode);
	}

}
