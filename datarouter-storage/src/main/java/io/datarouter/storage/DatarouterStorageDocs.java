/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage;

import io.datarouter.storage.client.ClientManager;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.index.MultiIndexReader;
import io.datarouter.storage.node.op.raw.QueueStorage;
import io.datarouter.storage.node.op.raw.read.IndexedStorageReader;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.QueueStorageReader;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.op.raw.read.TallyStorageReader;
import io.datarouter.storage.node.op.raw.write.IndexedStorageWriter;
import io.datarouter.storage.node.op.raw.write.MapStorageWriter;
import io.datarouter.storage.node.op.raw.write.QueueStorageWriter;
import io.datarouter.storage.node.op.raw.write.SortedStorageWriter;
import io.datarouter.storage.node.op.raw.write.StorageWriter;
import io.datarouter.storage.node.op.raw.write.TallyStorageWriter;
import io.datarouter.storage.node.type.physical.PhysicalNode;

/**
 * References to notable classes. Changes to the imports of this file indicate that README links should be updated.
 */
@SuppressWarnings("rawtypes")
public class DatarouterStorageDocs{

	// storing data
	ClientManager clientManager;
	Node node;
	PhysicalNode physicalNode;
	Dao dao;

	// datarouter management
	Datarouter datarouter;
	DatarouterClients clients;
	DatarouterNodes nodes;

	// storage types
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

	TallyStorageReader tallyStorageReader;
	TallyStorageWriter tallyStorageWriter;

}
