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

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.primary.RegularPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientNodeFactory;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.client.imp.QueueClientNodeFactory;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.entity.DefaultEntity;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.setting.Setting;

public abstract class BaseNodeFactory{

	private final DatarouterClients clients;
	private final DatarouterInjector injector;

	public BaseNodeFactory(DatarouterClients clients, DatarouterInjector injector){
		this.clients = clients;
		this.injector = injector;
	}

	protected abstract Setting<Boolean> getRecordCallsites();

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends PhysicalNode<PK,D,F>>
	N create(NodeParams<PK,D,F> params){
		ClientType<?,?> clientType = getClientTypeInstance(params.getClientId());
		ClientNodeFactory clientNodeFactory = getClientFactories(clientType);
		return cast(clientNodeFactory.createWrappedNode(params));
	}

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createSubEntity(EntityNodeParams<PK,DefaultEntity<PK>> entityNodeParams, NodeParams<PK,D,F> params){
		ClientType<?,?> clientType = getClientTypeInstance(params.getClientId());
		ClientNodeFactory clientNodeFactory = getClientFactories(clientType);
		return cast(clientNodeFactory.createWrappedSubEntityNode(entityNodeParams, params));
	}

	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N subEntityNode(//specify entityName and entityNodePrefix
			EntityNodeParams<EK,E> entityNodeParams,
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			String entityNodePrefix){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withParentName(entityNodeParams.getNodeName())
				.withEntity(entityNodeParams.getEntityTableName(), entityNodePrefix)
				.withDiagnostics(getRecordCallsites());
		return createSubEntityNode(entityNodeParams, clientId, paramsBuilder);
	}

	//specify entityName, entityNodePrefix and tableName
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N subEntityNode(
			EntityNodeParams<EK,E> entityNodeParams,
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			String entityNodePrefix,
			String tableName){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withParentName(entityNodeParams.getNodeName())
				.withEntity(entityNodeParams.getEntityTableName(), entityNodePrefix)
				.withDiagnostics(getRecordCallsites())
				.withTableName(tableName);
		return createSubEntityNode(entityNodeParams, clientId, paramsBuilder);
	}

	private <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createSubEntityNode(
			EntityNodeParams<EK,E> entityNodeParams,
			ClientId clientId,
			NodeParamsBuilder<PK,D,F> paramsBuilder){
		NodeParams<PK,D,F> nodeParams = paramsBuilder.build();
		ClientType<?,?> clientType = getClientTypeInstance(clientId);
		ClientNodeFactory clientNodeFactory = getClientFactories(clientType);
		return cast(clientNodeFactory.createWrappedSubEntityNode(entityNodeParams, nodeParams));
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
		ClientType<?,?> clientType = getClientTypeInstance(clientId);
		QueueClientNodeFactory clientFactories = (QueueClientNodeFactory) getClientFactories(clientType);
		return cast(clientFactories.createSingleQueueNode(params));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createGroupQueueNode(ClientId clientId, Supplier<D> databeanSupplier, String queueName,
			Supplier<F> fielderSupplier, String namespace, String queueUrl){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withTableName(queueName)
				.withNamespace(namespace)
				.withQueueUrl(queueUrl)
				.build();
		ClientType<?,?> clientType = getClientTypeInstance(clientId);
		QueueClientNodeFactory clientFactories = (QueueClientNodeFactory) getClientFactories(clientType);
		return cast(clientFactories.createGroupQueueNode(params));
	}

	private ClientType<?,?> getClientTypeInstance(ClientId clientId){
		return clients.getClientTypeInstance(clientId);
	}

	private ClientNodeFactory getClientFactories(ClientType<?,?> clientType){
		return injector.getInstance(clientType.getClientNodeFactoryClass());
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
