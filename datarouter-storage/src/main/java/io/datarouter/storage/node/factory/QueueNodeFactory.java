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

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.client.imp.QueueClientType;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.type.physical.PhysicalNode;

@Singleton
public class QueueNodeFactory{

	private final DatarouterClients clients;

	@Inject
	public QueueNodeFactory(DatarouterClients clients){
		this.clients = clients;
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
		N createSingleQueueNode(ClientId clientId, Supplier<D> databeanSupplier, String queueName,
				Supplier<F> fielderSupplier, boolean addAdapter, String namespace, String queueUrl){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withTableName(queueName)
				.withNamespace(namespace)
				.withQueueUrl(queueUrl)
				.build();
		QueueClientType clientType = getClientType(params);
		return wrapWithAdapterIfNecessary(clientType, clientType.createSingleQueueNode(params), addAdapter, params);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createSingleQueueNode(ClientId clientId, Supplier<D> databeanSupplier, String queueName,
			Supplier<F> fielderSupplier, boolean addAdapter, String namespace){
		return createSingleQueueNode(clientId, databeanSupplier, queueName, fielderSupplier, addAdapter, namespace,
				null);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createSingleQueueNode(ClientId clientId, Supplier<D> databeanSupplier, String queueName,
			Supplier<F> fielderSupplier, boolean addAdapter){
		return createSingleQueueNode(clientId, databeanSupplier, queueName, fielderSupplier, addAdapter, null, null);
	}

	/*------------------- GroupQueue -----------------*/

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
		N createGroupQueueNode(ClientId clientId, Supplier<D> databeanSupplier, String queueName,
				Supplier<F> fielderSupplier, boolean addAdapter, String namespace, String queueUrl){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withNamespace(namespace)
				.withTableName(queueName)
				.withQueueUrl(queueUrl)
				.build();
		QueueClientType clientType = getClientType(params);
		return wrapWithAdapterIfNecessary(clientType, clientType.createGroupQueueNode(params), addAdapter, params);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createGroupQueueNode(ClientId clientId, Supplier<D> databeanSupplier, String queueName,
			Supplier<F> fielderSupplier, boolean addAdapter){
		return createGroupQueueNode(clientId, databeanSupplier, queueName, fielderSupplier, addAdapter, null, null);
	}

	/*---------------- private ---------------------*/

	private QueueClientType getClientType(NodeParams<?,?,?> params){
		String clientName = params.getClientId().getName();
		ClientType clientType = clients.getClientTypeInstance(clientName);
		if(clientType == null){
			throw new NullPointerException("clientType not found for clientName:" + clientName);
		}
		return (QueueClientType) clientType;
	}

	@SuppressWarnings("unchecked")
	private static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N wrapWithAdapterIfNecessary(ClientType clientType, PhysicalNode<PK,D,F> node, boolean addAdapter,
			NodeParams<PK,D,F> params){
		if(addAdapter){
			return (N) clientType.createAdapter(params, node);
		}
		return (N) node;
	}
}
