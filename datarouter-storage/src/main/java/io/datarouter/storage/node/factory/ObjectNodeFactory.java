/**
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
package io.datarouter.storage.node.factory;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.client.imp.ObjectClientNodeFactory;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.Pathbean.PathbeanFielder;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.builder.ObjectNodeBuilder;
import io.datarouter.storage.node.op.raw.ObjectStorage.PhysicalObjectStorageNode;
import io.datarouter.storage.util.Subpath;

@Singleton
public class ObjectNodeFactory{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterClients clients;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	ObjectNodeBuilder<PK,D,F> create(
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		return new ObjectNodeBuilder<>(datarouter, this, clientId, databeanSupplier, fielderSupplier);
	}

	public <PK extends PrimaryKey<PK>,
			N extends PhysicalObjectStorageNode<PathbeanKey,Pathbean,PathbeanFielder>>
	N create(
			ClientId clientId,
			String bucketName,
			Subpath path){
		NodeParams<PathbeanKey,Pathbean,PathbeanFielder> params = new NodeParamsBuilder<>(
				Pathbean::new,
				PathbeanFielder::new)
				.withClientId(clientId)
				.withBucketName(bucketName)
				.withPath(path)
				.build();
		ClientType<?,?> clientType = clients.getClientTypeInstance(clientId);
		ObjectClientNodeFactory clientFactories = getClientFactories(clientType);
		return BaseNodeFactory.cast(clientFactories.createObjectNode(params));
	}

	public PhysicalObjectStorageNode<
			PathbeanKey,
			Pathbean,
			PathbeanFielder>
	createSubdirectory(
			PhysicalObjectStorageNode<PathbeanKey,Pathbean,PathbeanFielder> node,
			Subpath subdirectoryPath){
		Subpath fullPath = node.getRootPath().append(subdirectoryPath);
		return create(
				node.getClientId(),
				node.getBucket(),
				fullPath);

	}

	private ObjectClientNodeFactory getClientFactories(ClientType<?,?> clientType){
		return (ObjectClientNodeFactory) injector.getInstance(clientType.getClientNodeFactoryClass());
	}

}
