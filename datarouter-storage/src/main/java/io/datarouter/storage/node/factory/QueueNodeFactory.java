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

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientNodeFactory;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.client.imp.QueueClientNodeFactory;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.builder.GroupQueueNodeBuilder;
import io.datarouter.storage.node.builder.QueueNodeBuilder;
import io.datarouter.storage.node.builder.SingleQueueNodeBuilder;

public class QueueNodeFactory{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterClients clients;

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
			String queueUrl){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withTableName(queueName)
				.withNamespace(namespace)
				.withQueueUrl(queueUrl)
				.build();
		ClientType<?,?> clientType = getClientTypeInstance(clientId);
		QueueClientNodeFactory clientFactories = (QueueClientNodeFactory) getClientFactories(clientType);
		return BaseNodeFactory.cast(clientFactories.createSingleQueueNode(params));
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
			String queueUrl){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withTableName(queueName)
				.withNamespace(namespace)
				.withQueueUrl(queueUrl)
				.build();
		ClientType<?,?> clientType = getClientTypeInstance(clientId);
		QueueClientNodeFactory clientFactories = (QueueClientNodeFactory) getClientFactories(clientType);
		return BaseNodeFactory.cast(clientFactories.createGroupQueueNode(params));
	}

	private ClientType<?,?> getClientTypeInstance(ClientId clientId){
		return clients.getClientTypeInstance(clientId);
	}

	private ClientNodeFactory getClientFactories(ClientType<?,?> clientType){
		return injector.getInstance(clientType.getClientNodeFactoryClass());
	}

}
