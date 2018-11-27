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
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.client.imp.QueueClientNodeFactory;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;

@Singleton
public class QueueNodeFactory{

	private final DatarouterClients clients;
	private final DatarouterInjector injector;

	@Inject
	public QueueNodeFactory(DatarouterClients clients, DatarouterInjector injector){
		this.clients = clients;
		this.injector = injector;
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
		N createSingleQueueNode(ClientId clientId, Supplier<D> databeanSupplier, String queueName,
				Supplier<F> fielderSupplier, String namespace, String queueUrl){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withTableName(queueName)
				.withNamespace(namespace)
				.withQueueUrl(queueUrl)
				.build();
		QueueClientNodeFactory clientFactories = getClientFactories(params);
		return cast(clientFactories.createSingleQueueNode(params));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createSingleQueueNode(ClientId clientId, Supplier<D> databeanSupplier, String queueName,
			Supplier<F> fielderSupplier, String namespace){
		return createSingleQueueNode(clientId, databeanSupplier, queueName, fielderSupplier, namespace,
				null);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createSingleQueueNode(ClientId clientId, Supplier<D> databeanSupplier, String queueName,
			Supplier<F> fielderSupplier){
		return createSingleQueueNode(clientId, databeanSupplier, queueName, fielderSupplier, null, null);
	}

	/*------------------- GroupQueue -----------------*/

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
		N createGroupQueueNode(ClientId clientId, Supplier<D> databeanSupplier, String queueName,
				Supplier<F> fielderSupplier, String namespace, String queueUrl){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withNamespace(namespace)
				.withTableName(queueName)
				.withQueueUrl(queueUrl)
				.build();
		QueueClientNodeFactory clientFactories = getClientFactories(params);
		return cast(clientFactories.createGroupQueueNode(params));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createGroupQueueNode(ClientId clientId, Supplier<D> databeanSupplier, String queueName,
			Supplier<F> fielderSupplier){
		return createGroupQueueNode(clientId, databeanSupplier, queueName, fielderSupplier, null, null);
	}

	private QueueClientNodeFactory getClientFactories(NodeParams<?,?,?> params){
		String clientName = params.getClientId().getName();
		ClientType<?> clientType = clients.getClientTypeInstance(clientName);
		if(clientType == null){
			throw new NullPointerException("clientType not found for clientName:" + clientName);
		}
		return (QueueClientNodeFactory)injector.getInstance(clientType.getClientNodeFactoryClass());
	}

	/**
	 * Datarouter modules can come as runtime dependencies. There is no way to know at compile time if a client type
	 * is going to provide the desired interface, hence this unchecked cast.
	 */
	@SuppressWarnings("unchecked")
	private static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N cast(Node<PK,D,F> node){
		return (N)node;
	}

}
