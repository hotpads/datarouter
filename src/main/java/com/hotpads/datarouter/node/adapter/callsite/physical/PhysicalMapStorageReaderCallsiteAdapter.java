package com.hotpads.datarouter.node.adapter.callsite.physical;

import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.MapStorageReaderCallsiteAdapter;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.PhysicalMapStorageReaderNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;

public class PhysicalMapStorageReaderCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		PN extends PhysicalMapStorageReaderNode<PK,D>>
extends MapStorageReaderCallsiteAdapter<PK,D,F,PN>
implements PhysicalMapStorageReaderNode<PK,D>{

	
	public PhysicalMapStorageReaderCallsiteAdapter(NodeParams<PK,D,F> params, PN backingNode){		
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
