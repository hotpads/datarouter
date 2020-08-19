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
import io.datarouter.storage.client.ClientNodeFactory;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.client.imp.TallyClientNodeFactory;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.builder.TallyNodeBuilder;

@Singleton
public class TallyNodeFactory{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private DatarouterClients clients;
	@Inject
	private Datarouter datarouter;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	TallyNodeBuilder<PK,D,F> createTally(
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		return new TallyNodeBuilder<>(datarouter, this, clientId, databeanSupplier, fielderSupplier);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createTallyNode(
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			int version,
			String tableName,
			boolean isSystemTable){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withSchemaVersion(version)
				.withTableName(tableName)
				.withIsSystemTable(isSystemTable)
				.build();
		ClientType<?,?> clientType = getClientTypeInstance(clientId);
		TallyClientNodeFactory clientFactories = (TallyClientNodeFactory) getClientFactories(clientType);
		return BaseNodeFactory.cast(clientFactories.createTallyNode(params));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createTallyNode(NodeParams<PK,D,F> params){
		ClientType<?,?> clientType = getClientTypeInstance(params.getClientId());
		TallyClientNodeFactory clientFactories = (TallyClientNodeFactory)getClientFactories(clientType);
		return BaseNodeFactory.cast(clientFactories.createTallyNode(params));
	}

	private ClientType<?,?> getClientTypeInstance(ClientId clientId){
		return clients.getClientTypeInstance(clientId);
	}

	private ClientNodeFactory getClientFactories(ClientType<?,?> clientType){
		return injector.getInstance(clientType.getClientNodeFactoryClass());
	}

}
