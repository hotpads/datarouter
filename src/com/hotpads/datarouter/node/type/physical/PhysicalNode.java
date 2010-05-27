package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public interface PhysicalNode<D extends Databean,PK extends PrimaryKey<D>>
extends Node<D,PK>{
	
	public String getClientName();
	Client getClient();
	
	String getPhysicalName();
	String getPackagedPhysicalName();
}
