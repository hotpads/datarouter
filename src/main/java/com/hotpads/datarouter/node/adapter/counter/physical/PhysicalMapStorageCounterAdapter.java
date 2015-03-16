package com.hotpads.datarouter.node.adapter.counter.physical;

import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.node.adapter.counter.MapStorageCounterAdapter;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;

public class PhysicalMapStorageCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		PN extends PhysicalMapStorageNode<PK,D>>
extends MapStorageCounterAdapter<PK,D,F,PN>
implements PhysicalNode<PK,D>{

	
	public PhysicalMapStorageCounterAdapter(PN backingNode){		
		super(backingNode);
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
