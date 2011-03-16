package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PhysicalNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends Node<PK,D>{
	
	public String getClientName();
	Client getClient();
	
	String getTableName();
	String getPackagedTableName();
}
