package com.hotpads.datarouter.node.type.physical;

import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;

public interface PhysicalNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends Node<PK,D>{
	
	public String getClientName();
	Client getClient();
	
	String getTableName();
	String getPackagedTableName();
	
	<IK extends PrimaryKey<IK>, 
	IE extends IndexEntry<IK, IE, PK, D>, 
	IF extends DatabeanFielder<IK, IE>, 
	N extends ManagedNode<IK, IE, IF>> N registerManaged(N managedNode);
	
	<IK extends PrimaryKey<IK>, 
	IE extends IndexEntry<IK, IE, PK, D>, 
	IF extends DatabeanFielder<IK, IE>> List<ManagedNode<IK, IE, IF>> getManagedNodes();
}
