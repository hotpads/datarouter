package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

/**
 * A PhysicalNode is a node backed by a specific location like a database table, a memcached namespace, an in-memory
 * collection, or a remote API endpoint.  It is therefore tied to a specific Client, and a table accessible through that
 * Client.  By default, Datarouter will name the backing database table after the Databean it stores, but a PhysicalNode
 * can override the table name via getTableName().
 */
public interface PhysicalNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends Node<PK,D>{
	
	public ClientId getClientId();
	Client getClient();
	
	String getTableName();
	String getPackagedTableName();
	
}
