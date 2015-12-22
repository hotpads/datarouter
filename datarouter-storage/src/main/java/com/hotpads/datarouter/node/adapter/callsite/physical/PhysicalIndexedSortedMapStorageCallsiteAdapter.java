package com.hotpads.datarouter.node.adapter.callsite.physical;

import java.util.List;

import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.callsite.IndexedSortedMapStorageCallsiteAdapter;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;

public class PhysicalIndexedSortedMapStorageCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		PN extends PhysicalIndexedSortedMapStorageNode<PK,D>>
extends IndexedSortedMapStorageCallsiteAdapter<PK,D,F,PN>
implements PhysicalIndexedSortedMapStorageNode<PK,D>, PhysicalAdapterMixin<PK,D,PN>{


	public PhysicalIndexedSortedMapStorageCallsiteAdapter(NodeParams<PK,D,F> params, PN backingNode){
		super(params, backingNode);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		return backingNode.registerManaged(managedNode);
	}

	@Override
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return backingNode.getManagedNodes();
	}

}
