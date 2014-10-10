package com.hotpads.datarouter.node.type.physical;

import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PhysicalNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends Node<PK,D>{
	
	public String getClientName();
	Client getClient();
	
	String getTableName();
	String getPackagedTableName();
	
	<N extends ManagedNode> N registerManaged(N managedNode);
	List<ManagedNode> getManagedNodes();
}
