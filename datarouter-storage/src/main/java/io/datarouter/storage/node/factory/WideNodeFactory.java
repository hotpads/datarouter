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
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.RegularPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientNodeFactory;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.setting.DatarouterCallsiteSettings;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.builder.WideNodeBuilder;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.tableconfig.TableConfiguration;

public class WideNodeFactory{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterClients clients;
	@Inject
	private DatarouterCallsiteSettings datarouterCallsiteSettings;

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	WideNodeBuilder<PK,D,F> createWide(
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		return new WideNodeBuilder<>(datarouter, this, datarouterCallsiteSettings, clientId, databeanSupplier,
				fielderSupplier);
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
				.withDiagnostics(datarouterCallsiteSettings.getRecordCallsites());
		return createSubEntity(entityNodeParams, paramsBuilder.build());
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
				.withDiagnostics(datarouterCallsiteSettings.getRecordCallsites())
				.withTableName(tableName);
		return createSubEntity(entityNodeParams, paramsBuilder.build());
	}

	//specify entityName, entityNodePrefix tableName, and tableConfiguration
	@Deprecated
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
			String tableName,
			TableConfiguration tableConfiguration){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withParentName(entityNodeParams.getNodeName())
				.withEntity(entityNodeParams.getEntityTableName(), entityNodePrefix)
				.withDiagnostics(datarouterCallsiteSettings.getRecordCallsites())
				.withTableName(tableName)
				.withTableConfiguration(tableConfiguration);
		return createSubEntity(entityNodeParams, paramsBuilder.build());
	}

	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createSubEntity(
			EntityNodeParams<EK,E> entityNodeParams,
			NodeParams<PK,D,F> nodeParams){
		ClientType<?,?> clientType = getClientTypeInstance(nodeParams.getClientId());
		ClientNodeFactory clientNodeFactory = getClientFactories(clientType);
		return BaseNodeFactory.cast(clientNodeFactory.createWrappedSubEntityNode(entityNodeParams, nodeParams));
	}

	/*-------------- private -----------------*/

	private ClientType<?,?> getClientTypeInstance(ClientId clientId){
		return clients.getClientTypeInstance(clientId);
	}

	private ClientNodeFactory getClientFactories(ClientType<?,?> clientType){
		return injector.getInstance(clientType.getClientNodeFactoryClass());
	}

}
