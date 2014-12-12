package com.hotpads.datarouter;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

/**
 * A starting point for discovering key classes.
 * 
 * @author mcorgan
 *
 */
public class DatarouterDocs{

	//data model
	PrimaryKey primaryKey;
	Databean databean;
	Field field;
	Fielder fielder;
	EntityKey entityKey;
	Entity entity;
	
	//storing data
	Client client;
	Node node;
	DataRouter datarouter;
	
	//datarouter management
	DataRouterContext drContext;
	Clients clients;
	Nodes nodes;
	
	//storage types
	MapStorageReader mapStorageReader;
	MapStorageWriter mapStorageWriter;
	SortedStorageReader sortedStorageReader;
	SortedStorageWriter sortedStorageWriter;
	
}
