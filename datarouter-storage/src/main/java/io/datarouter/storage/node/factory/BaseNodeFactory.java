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
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientNodeFactory;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.builder.NodeBuilder;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public abstract class BaseNodeFactory{

	private final Datarouter datarouter;
	private final DatarouterClients clients;
	private final DatarouterInjector injector;
	private final Supplier<Boolean> enableDiagnosticsSupplier;

	public BaseNodeFactory(
			Datarouter datarouter,
			DatarouterClients clients,
			DatarouterInjector injector,
			Supplier<Boolean> enableDiagnosticsSupplier){
		this.datarouter = datarouter;
		this.clients = clients;
		this.injector = injector;
		this.enableDiagnosticsSupplier = enableDiagnosticsSupplier;
	}

	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends PhysicalNode<PK,D,F>>
	N create(
			EntityNodeParams<EK,E> entityNodeParams,
			NodeParams<PK,D,F> params){
		ClientType<?,?> clientType = getClientTypeInstance(params.getClientId());
		ClientNodeFactory clientNodeFactory = getClientFactories(clientType);
		return cast(clientNodeFactory.createWrappedNode(entityNodeParams, params));
	}

	public <EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	NodeBuilder<EK,PK,D,F> create(
			ClientId clientId,
			Supplier<EK> entityKeySupplier,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		return new NodeBuilder<>(this, enableDiagnosticsSupplier, clientId, entityKeySupplier,
				databeanSupplier, fielderSupplier);
	}

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	NodeBuilder<PK,PK,D,F> create(
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		Supplier<PK> entityKeySupplier = databeanSupplier.get().getKeySupplier();
		return create(clientId, entityKeySupplier, databeanSupplier, fielderSupplier);
	}

	public <EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends PhysicalNode<PK,D,F>>
	N register(N node){
		return datarouter.register(node);
	}

	/*-------------- private -----------------*/

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
	public static <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N cast(Node<PK,D,F> node){
		return (N)node;
	}

}
