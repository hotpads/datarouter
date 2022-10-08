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
package io.datarouter.filesystem.client;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.bytes.Codec;
import io.datarouter.filesystem.node.object.DirectoryBlobStorage;
import io.datarouter.filesystem.node.object.DirectoryBlobStorageNode;
import io.datarouter.filesystem.node.queue.DirectoryGroupQueueNode;
import io.datarouter.filesystem.node.queue.DirectoryQueueNode;
import io.datarouter.filesystem.raw.DirectoryManager;
import io.datarouter.filesystem.raw.DirectoryManager.DirectoryManagerFactory;
import io.datarouter.filesystem.raw.queue.DirectoryQueue;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.EmptyDatabean;
import io.datarouter.model.databean.EmptyDatabean.EmptyDatabeanFielder;
import io.datarouter.model.key.EmptyDatabeanKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.imp.BlobClientNodeFactory;
import io.datarouter.storage.client.imp.BlobQueueClientNodeFactory;
import io.datarouter.storage.client.imp.QueueClientNodeFactory;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.NodeAdapters;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.PhysicalBlobQueueStorageNode;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.string.StringTool;

@Singleton
public class FilesystemClientNodeFactory
implements BlobClientNodeFactory, BlobQueueClientNodeFactory, QueueClientNodeFactory{

	@Inject
	private FilesystemClientType clientType;
	@Inject
	private ServiceName serviceName;
	@Inject
	private FilesystemNodeFactory filesystemNodeFactory;
	@Inject
	private DirectoryManagerFactory directoryManagerFactory;
	@Inject
	private FilesystemOptions filesystemOptions;
	@Inject
	private NodeAdapters nodeAdapters;

	/*-------------- BlobClientNodeFactory --------------*/

	@Override
	public PhysicalBlobStorageNode createBlobNode(
			NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> nodeParams){
		DirectoryBlobStorage directoryBlobStorage = makeDirectoryObjectStorage(nodeParams);
		var node = new DirectoryBlobStorageNode(
				nodeParams,
				clientType,
				directoryBlobStorage,
				nodeParams.getPhysicalName(),
				nodeParams.getPath());
		return nodeAdapters.wrapBlobNode(node);
	}

	/*-------------- BlobQueueClientNodeFactory --------------*/

	@Override
	public <T> PhysicalBlobQueueStorageNode<T> createBlobQueueNode(
			NodeParams<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder> nodeParams,
			Codec<T,byte[]> codec){
		var node = filesystemNodeFactory.createBlobNode(
				makeDirectoryQueue(nodeParams),
				nodeParams,
				codec);
		return nodeAdapters.wrapBlobQueueNode(node);
	}

	/*-------------- QueueClientNodeFactory --------------*/

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createSingleQueueNode(NodeParams<PK,D,F> nodeParams){
		DirectoryQueueNode<PK,D,F> node = filesystemNodeFactory.createSingleNode(
				makeDirectoryQueue(nodeParams),
				nodeParams);
		return nodeAdapters.wrapQueueNode(node);
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createGroupQueueNode(NodeParams<PK,D,F> nodeParams){
		DirectoryGroupQueueNode<PK,D,F> node = filesystemNodeFactory.createGroupNode(
				makeDirectoryQueue(nodeParams),
				nodeParams);
		return nodeAdapters.wrapGroupQueueNode(node);
	}

	/*---------------- private -------------------*/

	private DirectoryBlobStorage makeDirectoryObjectStorage(NodeParams<?,?,?> nodeParams){
		Path rootPath = filesystemOptions.getRoot(nodeParams.getClientName());
		String relativePathString = Scanner.of(nodeParams.getPhysicalName(), nodeParams.getPath().toString())
				.exclude(Objects::isNull)
				.collect(Collectors.joining("/"));
		Path relativePath = Paths.get(relativePathString);
		Path fullPath = rootPath.resolve(relativePath);
		DirectoryManager directoryManager = directoryManagerFactory.create(fullPath.toString());
		return new DirectoryBlobStorage(directoryManager);
	}

	private DirectoryQueue makeDirectoryQueue(NodeParams<?,?,?> nodeParams){
		Path rootPath = filesystemOptions.getRoot(nodeParams.getClientName());
		String relativePathString = Scanner.of(serviceName.get(), nodeParams.getPhysicalName())
				.exclude(StringTool::isEmpty)
				.collect(Collectors.joining("/"));
		Path relativePath = Paths.get(relativePathString);
		Path fullPath = rootPath.resolve(relativePath);
		DirectoryManager directoryManager = directoryManagerFactory.create(fullPath.toString());
		return new DirectoryQueue(directoryManager);
	}

}
