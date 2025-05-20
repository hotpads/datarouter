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
package io.datarouter.storage.node.factory;

import java.time.Duration;
import java.util.function.Supplier;

import io.datarouter.bytes.Codec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.EmptyDatabean;
import io.datarouter.model.databean.EmptyDatabean.EmptyDatabeanFielder;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.imp.BlobQueueClientNodeFactory;
import io.datarouter.storage.client.imp.QueueClientNodeFactory;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.builder.BlobQueueNodeBuilder;
import io.datarouter.storage.node.builder.GroupQueueNodeBuilder;
import io.datarouter.storage.node.builder.QueueNodeBuilder;
import io.datarouter.storage.node.builder.SingleQueueNodeBuilder;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.BlobQueueStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.Require;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class QueueNodeFactory extends BaseNodeFactory{

	@Inject
	private Datarouter datarouter;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	QueueNodeBuilder<PK,D,F> createSingleQueue(
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		return new SingleQueueNodeBuilder<>(datarouter, this, clientId, databeanSupplier, fielderSupplier);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	QueueNodeBuilder<PK,D,F> createGroupQueue(
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		return new GroupQueueNodeBuilder<>(datarouter, this, clientId, databeanSupplier, fielderSupplier);
	}

	public <T> BlobQueueNodeBuilder<T> createBlobQueue(ClientId clientId, String queueName, Codec<T,byte[]> codec){
		return new BlobQueueNodeBuilder<>(datarouter, this, clientId, queueName, codec);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createSingleQueueNode(
			ClientId clientId,
			Supplier<D> databeanSupplier,
			String queueName,
			Supplier<F> fielderSupplier,
			String namespace,
			String queueUrl,
			Tag tag,
			Duration customMessageAgeThreshold){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withTableName(queueName)
				.withNamespace(namespace)
				.withQueueUrl(queueUrl)
				.withTag(tag)
				.withCustomMessageAgeThreshold(customMessageAgeThreshold)
				.build();
		QueueClientNodeFactory clientFactories = getClientNodeFactory(clientId, QueueClientNodeFactory.class);
		return cast(clientFactories.createSingleQueueNode(params));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createGroupQueueNode(
			ClientId clientId,
			Supplier<D> databeanSupplier,
			String queueName,
			Supplier<F> fielderSupplier,
			String namespace,
			String queueUrl,
			Tag tag,
			Duration customMessageAgeThreshold){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withTableName(queueName)
				.withNamespace(namespace)
				.withQueueUrl(queueUrl)
				.withTag(tag)
				.withCustomMessageAgeThreshold(customMessageAgeThreshold)
				.build();
		QueueClientNodeFactory clientFactories = getClientNodeFactory(clientId, QueueClientNodeFactory.class);
		return cast(clientFactories.createGroupQueueNode(params));
	}

	public <T> BlobQueueStorageNode<T> createBlobQueueNode(
			ClientId clientId,
			String queueName,
			Codec<T,byte[]> codec,
			String namespace,
			String queueUrl,//TODO remove? wait for other impls to decide on removing.
			Tag tag,
			Duration customMessageAgeThreshold){
		Require.notBlank(queueName);
		var params = new NodeParamsBuilder<>(EmptyDatabean::new, EmptyDatabeanFielder::new)
				.withClientId(clientId)
				.withTableName(queueName)
				.withNamespace(namespace)
				.withQueueUrl(queueUrl)
				.withTag(tag)
				.withCustomMessageAgeThreshold(customMessageAgeThreshold)
				.build();
		BlobQueueClientNodeFactory clientFactories = getClientNodeFactory(clientId, BlobQueueClientNodeFactory.class);
		return clientFactories.createBlobQueueNode(params, codec);
	}

}
