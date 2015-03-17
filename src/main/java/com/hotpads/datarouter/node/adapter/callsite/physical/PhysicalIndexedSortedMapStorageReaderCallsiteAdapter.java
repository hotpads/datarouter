package com.hotpads.datarouter.node.adapter.callsite.physical;

import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.IndexedSortedMapStorageReaderCallsiteAdapter;
import com.hotpads.datarouter.node.op.combo.reader.IndexedSortedMapStorageReader.PhysicalIndexedSortedMapStorageReaderNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;

public class PhysicalIndexedSortedMapStorageReaderCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		PN extends PhysicalIndexedSortedMapStorageReaderNode<PK,D>>
extends IndexedSortedMapStorageReaderCallsiteAdapter<PK,D,F,PN>
implements PhysicalIndexedSortedMapStorageReaderNode<PK,D>{

	
	public PhysicalIndexedSortedMapStorageReaderCallsiteAdapter(NodeParams<PK,D,F> params, PN backingNode){		
		super(params, backingNode);
	}
	
	
	//PhysicalNode pass-through methods are copy/pasted to all nodes in this package
	/********************** PhysicalNode  ****************************/

	@Override
	public String getClientName(){
		return backingNode.getClientName();
	}

	@Override
	public Client getClient(){
		return backingNode.getClient();
	}

	@Override
	public String getTableName(){
		return backingNode.getTableName();
	}

	@Override
	public String getPackagedTableName(){
		return backingNode.getPackagedTableName();
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<IK,IE,IF>>
	MN registerManaged(MN managedNode){
		return backingNode.registerManaged(managedNode);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<ManagedNode<IK,IE,IF>> getManagedNodes(){
		return backingNode.getManagedNodes();
	}

	
}
