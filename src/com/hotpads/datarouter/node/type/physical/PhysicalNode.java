package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.databean.Databean;

public interface PhysicalNode<D extends Databean>
extends Node<D>{
	
	public String getClientName();
	Client getClient();
	
	String getPhysicalName();
	String getPackagedPhysicalName();
}
