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
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.filesystem.node.object.DirectoryBlobStorage;
import io.datarouter.filesystem.node.object.DirectoryBlobStorageNode;
import io.datarouter.filesystem.node.queue.DirectoryGroupQueueNode;
import io.datarouter.filesystem.node.queue.DirectoryQueueNode;
import io.datarouter.filesystem.raw.DirectoryManager;
import io.datarouter.filesystem.raw.DirectoryManager.DirectoryManagerFactory;
import io.datarouter.filesystem.raw.queue.DirectoryQueue;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.imp.BaseClientNodeFactory;
import io.datarouter.storage.client.imp.BlobClientNodeFactory;
import io.datarouter.storage.client.imp.QueueClientNodeFactory;
import io.datarouter.storage.client.imp.WrappedNodeFactory;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalGroupQueueStorageCounterAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalQueueStorageCounterAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalGroupQueueStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalQueueStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalGroupQueueStorageTraceAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalQueueStorageTraceAdapter;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.op.raw.QueueStorage.PhysicalQueueStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.web.config.service.ServiceName;

@Singleton
public class FilesystemClientNodeFactory
extends BaseClientNodeFactory
implements BlobClientNodeFactory, QueueClientNodeFactory{

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

	public class FilesystemWrappedNodeFactory<
			EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends WrappedNodeFactory<EK,E,PK,D,F,PhysicalQueueStorageNode<PK,D,F>>{

		@Override
		public PhysicalQueueStorageNode<PK,D,F> createNode(
				EntityNodeParams<EK,E> entityNodeParams,
				NodeParams<PK,D,F> nodeParams){
			return filesystemNodeFactory.createSingleNode(makeDirectoryQueue(nodeParams), nodeParams);
		}

		@Override
		public List<UnaryOperator<PhysicalQueueStorageNode<PK,D,F>>> getAdapters(){
			return List.of(
					PhysicalQueueStorageCounterAdapter::new,
					PhysicalQueueStorageSanitizationAdapter::new,
					PhysicalQueueStorageTraceAdapter::new);
		}

	}

	@Override
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	WrappedNodeFactory<EK,E,PK,D,F,PhysicalQueueStorageNode<PK,D,F>> makeWrappedNodeFactory(){
		return new FilesystemWrappedNodeFactory<>();
	}

	/*-------------- ObjectClientNodeFactory --------------*/

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createBlobNode(NodeParams<PK,D,F> nodeParams){
		DirectoryBlobStorage directoryBlobStorage = makeDirectoryObjectStorage(nodeParams);
		return new DirectoryBlobStorageNode<>(
				nodeParams,
				clientType,
				directoryBlobStorage,
				nodeParams.getPhysicalName(),
				nodeParams.getPath());
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
		return new PhysicalQueueStorageTraceAdapter<>(
				new PhysicalQueueStorageCounterAdapter<>(
				new PhysicalQueueStorageSanitizationAdapter<>(node)));
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createGroupQueueNode(NodeParams<PK,D,F> nodeParams){
		DirectoryGroupQueueNode<PK,D,F> node = filesystemNodeFactory.createGroupNode(
				makeDirectoryQueue(nodeParams),
				nodeParams);
		return new PhysicalGroupQueueStorageTraceAdapter<>(
				new PhysicalGroupQueueStorageCounterAdapter<>(
				new PhysicalGroupQueueStorageSanitizationAdapter<>(node)));
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
		String relativePathString = String.join("/", serviceName.get(), nodeParams.getPhysicalName());
		Path relativePath = Paths.get(relativePathString);
		Path fullPath = rootPath.resolve(relativePath);
		DirectoryManager directoryManager = directoryManagerFactory.create(fullPath.toString());
		return new DirectoryQueue(directoryManager);
	}

}
