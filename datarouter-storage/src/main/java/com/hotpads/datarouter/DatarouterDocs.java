package com.hotpads.datarouter;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.DatarouterNodes;
import com.hotpads.datarouter.node.op.index.MultiIndexReader;
import com.hotpads.datarouter.node.op.raw.QueueStorage;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.QueueStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.QueueStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.StorageWriter;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

/**
 * A starting point for discovering key classes.
 */
@SuppressWarnings("rawtypes") 
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
	PhysicalNode physicalNode;
	Router router;
	
	//datarouter management
	Datarouter datarouter;
	DatarouterClients clients;
	DatarouterNodes nodes;
	
	//storage types
	StorageWriter storageWriter;
	
	MapStorageReader mapStorageReader;
	MapStorageWriter mapStorageWriter;
	
	SortedStorageReader sortedStorageReader;
	SortedStorageWriter sortedStorageWriter;

	QueueStorageWriter queueStorageWriter;
	QueueStorageReader queueStorageReader;
	QueueStorage queueStorage;

	MultiIndexReader multiIndexReader;
	
	IndexedStorageReader indexedStorageReader;
	IndexedStorageWriter indexedStorageWriter;
	
}
